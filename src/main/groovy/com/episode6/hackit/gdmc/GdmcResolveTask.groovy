package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ComponentMetadata
import org.gradle.api.artifacts.ComponentSelection
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class GdmcResolveTask extends DefaultTask {

  private static String CONFIG_NAME = "gdmcTemporaryConfig"

  List<String> keys
  boolean allowSnapshots = false

  private Set<GdmcDependency> resolvedDependencies

  @TaskAction
  def resolve() {

    // create a temporary config to resolve the requested dependencies
    def config = project.configurations.create(CONFIG_NAME) {

      // don't bother resolving dependencies of dependencies
      transitive = false
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
    keys.each {
      project.dependencies.add(config.name, "${it}:+")
    }

    // collect resolved depencies into the resolvedDependencies map
    resolvedDependencies = config.resolvedConfiguration.getFirstLevelModuleDependencies(Specs.SATISFIES_ALL).collect {
      GdmcDependency.from(it.module.id)
    }
  }

  Set<GdmcDependency> getResolvedDependencies() {
    if (resolvedDependencies == null) {
      throw new IllegalAccessException("Called getResolvedDependencies before they have been resolved.")
    }
    return resolvedDependencies
  }
}
