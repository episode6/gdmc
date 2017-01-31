package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency
import com.episode6.hackit.gdmc.task.GdmcResolveTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {

  DependencyMap mapper

  void apply(Project project) {
    GdmcRootPlugin rootPlugin = project.rootProject.plugins.findPlugin(GdmcRootPlugin)
    if (!rootPlugin) {
      rootPlugin = project.rootProject.plugins.apply(GdmcRootPlugin)
    }
    mapper = rootPlugin.dependencyMap

    project.configurations.all(new Action<Configuration>() {
      @Override
      void execute(Configuration files) {
        files.resolutionStrategy.eachDependency(
            new VersionMapperAction(dependencyMap: mapper, configuration: files, project: project))
      }
    })

    project.task("gdmcResolve", type: GdmcResolveTask) {
      keys = {
        return project.configurations.collectMany { Configuration config ->
          return config.dependencies.collectMany { Dependency dep ->
            GdmcDependency unMapped = GdmcDependency.from(dep)
            if (unMapped.version || mapper.lookup(unMapped.key)) {
              println "found valid dep: ${unMapped.toString()}"
              return []
            }
            println "found invalid dep: ${unMapped.key}"
            return [unMapped.key]
          }
        }
      }
      doLast {
        mapper.applyMissingDependencies(resolvedDependencies)
        resolvedDependencies.each { resolved ->
          println "RESOLVED VERSION ${resolved.key} -> ${resolved}"
        }
      }
    }
  }
}
