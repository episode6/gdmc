package com.episode6.hackit.gdmc

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

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

    project.configurations.all(new Action<Configuration>() {
      @Override
      void execute(Configuration files) {
        files.resolutionStrategy.eachDependency(
            new VersionMapperAction(dependencyMap: mapper, configuration: files, project: project))
      }
    })

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
