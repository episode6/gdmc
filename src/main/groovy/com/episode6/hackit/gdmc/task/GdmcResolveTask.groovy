package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.json.GdmcDependency
import org.gradle.api.artifacts.ComponentMetadata
import org.gradle.api.artifacts.ComponentSelection
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class GdmcResolveTask extends AbstractGdmcTask {

  private static String CONFIG_NAME = "gdmcTemporaryConfig"

  @Input
  Closure<Collection<GdmcDependency>> keys

  @Input
  boolean allowSnapshots = false

  @Input
  boolean resolveTransitive = false

  @TaskAction
  def resolve() {

    // create a temporary config to resolve the requested dependencies
    def config = project.configurations.create(CONFIG_NAME) {
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
    keys.call().each {
      String notation = it.version ? it.toString() : "${it.toString()}:+"
      project.dependencies.add(config.name, notation)
    }

    // collect resolved dependencies into a set
    Set<ResolvedDependency> resolvedDependencies = resolveTransitive ?
        config.resolvedConfiguration.lenientConfiguration.allModuleDependencies :
        config.resolvedConfiguration.getFirstLevelModuleDependencies(Specs.SATISFIES_ALL)

    writeJsonToOutputFile(resolvedDependencies.collect {GdmcDependency.from(it.module.id).toMap()})
  }
}
