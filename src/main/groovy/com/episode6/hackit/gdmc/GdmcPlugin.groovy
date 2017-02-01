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

  void apply(Project project) {
    if (!project.rootProject.plugins.findPlugin(GdmcRootPlugin)) {
      project.rootProject.plugins.apply(GdmcRootPlugin)
    }
    project.plugins.apply(GdmcTasksPlugin)

    project.configurations.all(new Action<Configuration>() {
      @Override
      void execute(Configuration files) {
        VersionMapperAction action = new VersionMapperAction(configuration: files, project: project)
        files.resolutionStrategy.eachDependency(action)
      }
    })

  }
}
