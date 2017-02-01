package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.task.GdmcTasksPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

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

    GdmcTasksPlugin.init(project)
  }
}
