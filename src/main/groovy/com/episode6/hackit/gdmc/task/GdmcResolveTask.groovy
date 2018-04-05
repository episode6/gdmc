package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.util.HasProjectTrait
import com.episode6.hackit.gdmc.util.TaskAssertions
import groovy.json.JsonBuilder
import groovy.transform.Memoized
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ComponentMetadata
import org.gradle.api.artifacts.ComponentSelection
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Task type to resolve dependencies (usually written to gdmc.json)
 */
class GdmcResolveTask extends DefaultTask implements HasProjectTrait {

  private static final String CONFIG_NAME_SUFFIX = "TemporaryConfig"

  @Input
  Closure<Collection<GdmcDependency>> dependencies

  @Input
  boolean allowSnapshots = false

  @Input
  boolean resolveTransitive = false

  @Input
  boolean useBuildScriptConfig = false

  @OutputFile @Memoized
  File getOutputFile() {
    return project.file("${project.buildDir}/${name}.json")
  }

  @TaskAction
  def resolve() {
    TaskAssertions.assertLonelyTask(this)

    GChop.d(
        "Starting GdmcResolveTask named: %s, allowSnapshots: %s, resolveTransitive: %s",
        name,
        allowSnapshots,
        resolveTransitive)

    def configContainer = useBuildScriptConfig ? project.buildscript.configurations : project.configurations
    def dependencyContainer = useBuildScriptConfig ? project.buildscript.dependencies : project.dependencies

    // create a temporary config to resolve the requested dependencies
    def config = configContainer.create("${name}${CONFIG_NAME_SUFFIX}") {
      transitive = resolveTransitive
    }

    // gradle includes snapshots by default, filter them out here
    if (!allowSnapshots) {
      config.resolutionStrategy { resolutionStrategy ->
        resolutionStrategy.componentSelection { rules ->
          rules.all { ComponentSelection selection, ComponentMetadata metadata ->
            if (metadata.status == 'integration') {
              selection.reject("Component status ${metadata.status} rejected")
            }
          }
        }
      }
    }

    // a map of mavenKey -> List<mapKey> for our input, will be used to create output Map
    Map<String, List<String>> mapKeysMap = new HashMap<>();

    // add query dependencies to new config
    dependencies.call().findAll {
      // ignore self and locked dependencies
      !it.matchesAnyProject(project) && !dependencyMap.isLocked(it.mapKey)
    }.each {
      // map this dependency's mapKey to its maven key
      mapKeysMap.get(it.mavenKey, new LinkedList<String>()).add(it.mapKey)

      GChop.d("Adding dependency: %s to config: %s", it.mapKey, config.name)
      String notation = it.version ? it.fullMavenKey : "${it.mavenKey}:+"
      dependencyContainer.add(config.name, notation)
    }

    // collect resolved dependencies into a set
    Set<ResolvedDependency> resolvedDependencies = resolveTransitive ?
        config.resolvedConfiguration.lenientConfiguration.allModuleDependencies :
        config.resolvedConfiguration.getFirstLevelModuleDependencies(Specs.SATISFIES_ALL)

    // output map that will be written to json
    Map<String, Map> outputMap = new HashMap<>();

    resolvedDependencies.each {
      def dep = GdmcDependency.from(it.module.id)
      def mavenKey = dep.mavenKey

      // for each mapKey that is mapped to this mavenKey, add a new entry
      // to the outputMap with the resolved dependency
      mapKeysMap.get(mavenKey, [mavenKey]).each { String mapKey ->
        outputMap.put(mapKey, dep.toMap())
      }
    }
    writeJsonToOutputFile(outputMap)
  }

  private void writeJsonToOutputFile(Object obj) {
    GChop.d("Writing to outputFile: %s content: %s", outputFile.absolutePath, obj)
    outputFile.text = new JsonBuilder(obj).toString()
  }
}
