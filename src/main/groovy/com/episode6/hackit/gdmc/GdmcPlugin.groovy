package com.episode6.hackit.gdmc

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {

  GdmcDependencyMapper rootPlugin

  void apply(Project project) {
    rootPlugin = project.rootProject.plugins.findPlugin(GdmcDependencyMapper)
    if (!rootPlugin) {
      rootPlugin = project.rootProject.plugins.apply(GdmcDependencyMapper)
    }

    project.task("gdmcResolve", type: GdmcResolveTask) {
      keys = dependencies.missingDependencies
      doLast {
        dependencies.applyChanges(resolvedDependencies)
        dependencies.writeToFile(rootPlugin.gdmcFile)
        resolvedDependencies.each { key, resolved ->
          println "RESOLVED VERSION ${key} -> ${resolved}"
        }
      }
    }
  }

  GdmcDependencyContainer getDependencies() {
    return rootPlugin.dependencies
  }
}
