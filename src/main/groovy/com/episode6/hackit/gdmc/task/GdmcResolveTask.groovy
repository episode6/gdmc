package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.json.GdmcDependency
import groovy.json.JsonBuilder
import groovy.transform.Memoized
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ComponentMetadata
import org.gradle.api.artifacts.ComponentSelection
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class GdmcResolveTask extends AbstractGdmcTask {

  private static String CONFIG_NAME = "gdmcTemporaryConfig"

  @Input
  Closure<Collection<String>> keys

  @Input
  boolean allowSnapshots = false

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
    keys.call().each {
      project.dependencies.add(config.name, "${it}:+")
    }

    // collect resolved depencies into the resolvedDependencies map
    Collection<Map> resolvedDependencies = config.resolvedConfiguration.getFirstLevelModuleDependencies(Specs.SATISFIES_ALL).collect {
      GdmcDependency.from(it.module.id).toMap()
    }

    writeJsonToOutputFile(resolvedDependencies)
  }
}
