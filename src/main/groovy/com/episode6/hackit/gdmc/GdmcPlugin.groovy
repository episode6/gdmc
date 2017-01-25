package com.episode6.hackit.gdmc

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {

  GdmcDependencyMapper mapper

  void apply(Project project) {
    mapper = project.rootProject.plugins.findPlugin(GdmcDependencyMapper)
    if (!mapper) {
      mapper = project.rootProject.plugins.apply(GdmcDependencyMapper)
    }

    project.task("gdmcResolve", type: GdmcResolveTask) {
      keys = mapper.missingDependencies
      doLast {
        mapper.applyMissingDependencies(resolvedDependencies)
        resolvedDependencies.each { resolved ->
          println "RESOLVED VERSION ${resolved.key} -> ${resolved}"
        }
      }
    }
  }
}
