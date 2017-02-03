package com.episode6.hackit.gdmc

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {

  void apply(Project project) {
    GdmcRootPlugin.ensureInit(project)
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
