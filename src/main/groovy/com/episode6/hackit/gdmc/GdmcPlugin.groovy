package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.throwable.GdmcPluginMissingException
import org.gradle.api.Nullable
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {

  GdmcRootProjectPlugin rootPlugin

  void apply(Project project) {
    rootPlugin = project.rootProject.plugins.findPlugin(GdmcRootProjectPlugin)
    if (!rootPlugin) {
      rootPlugin = project.rootProject.plugins.apply(GdmcRootProjectPlugin)
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
