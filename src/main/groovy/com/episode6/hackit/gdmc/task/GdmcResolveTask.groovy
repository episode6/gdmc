package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.exception.GdmcIllegalTaskGroupingException
import groovy.json.JsonBuilder
import groovy.transform.Memoized
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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
class GdmcResolveTask extends DefaultTask {

  private static final List<String> LEGAL_TASKS = ["clean"]

  private static final String CONFIG_NAME_SUFFIX = "TemporaryConfig"

  @Input
  Closure<Collection<GdmcDependency>> dependencies

  @Input
  boolean allowSnapshots = false

  @Input
  boolean resolveTransitive = false

  @OutputFile @Memoized
  File getOutputFile() {
    return project.file("${project.buildDir}/${name}.json")
  }

  @TaskAction
  def resolve() {
    assertLonelyTask()

    GChop.d(
        "Starting GdmcResolveTask named: %s, allowSnapshots: %s, resolveTransitive: %s",
        name,
        allowSnapshots,
        resolveTransitive)

    // create a temporary config to resolve the requested dependencies
    def config = project.configurations.create("${name}${CONFIG_NAME_SUFFIX}") {
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

    // add query dependencies to new config
    dependencies.call().each {
      GChop.d("Adding dependency: %s to config: %s", it, config.name)
      String notation = it.version ? it.toString() : "${it.toString()}:+"
      project.dependencies.add(config.name, notation)
    }

    // collect resolved dependencies into a set
    Set<ResolvedDependency> resolvedDependencies = resolveTransitive ?
        config.resolvedConfiguration.lenientConfiguration.allModuleDependencies :
        config.resolvedConfiguration.getFirstLevelModuleDependencies(Specs.SATISFIES_ALL)

    writeJsonToOutputFile(resolvedDependencies.collect {GdmcDependency.from(it.module.id).toMap()})
  }

  private void writeJsonToOutputFile(Object obj) {
    GChop.d("Writing to outputFile: %s content: %s", outputFile.absolutePath, obj)
    outputFile.text = new JsonBuilder(obj).toString()
  }

  /**
   * Ensure this task is being executed by itself
   */
  private void assertLonelyTask() {
    List<String> legalSuffixes = new LinkedList<>(LEGAL_TASKS)
    legalSuffixes.add(name)

    def illegalTask = project.gradle.taskGraph.allTasks.find { task ->
      legalSuffixes.find { legalSuffix ->
        task.name.endsWith(legalSuffix)
      } == null
    }

    if (illegalTask) {
      throw new GdmcIllegalTaskGroupingException(this, illegalTask)
    }
  }
}
