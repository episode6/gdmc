package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.util.VersionMapperAction
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

    VersionMapperAction versionMapper = new VersionMapperAction(project: project)
    project.configurations.all(new Action<Configuration>() {
      @Override
      void execute(Configuration config) {
        config.resolutionStrategy.eachDependency(versionMapper)
      }
    })

  }
}
