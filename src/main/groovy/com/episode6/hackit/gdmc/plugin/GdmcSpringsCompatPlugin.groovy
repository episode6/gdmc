package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.util.VersionMapperAction
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Plugin to make gdmc compatible with Spring's dependency management plugin
 * https://github.com/spring-gradle-plugins/dependency-management-plugin
 */
class GdmcSpringsCompatPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    DependencyMap dependencyMap = GdmcRootPlugin.ensureInit(project).dependencyMap
    project.plugins.apply(GdmcTasksPlugin)

    project.dependencyManagement {
      dependencies {
        dependencyMap.validDependencies.each {
          GChop.d("added dependency %s to dependncyManager", it)
          dependency it.toString()
        }
      }
    }

    // While we leave most of the dependency mapping up to the springs plugin, we must ensure any aliases
    // get mapped properly
    project.configurations.all(new Action<Configuration>() {
      @Override
      void execute(Configuration files) {
        VersionMapperAction action = new VersionMapperAction(configuration: files, project: project) {
          @Override
          boolean shouldSkipMappingVersion(GdmcDependency unMapped) {
            return !unMapped.isPlaceholder()
          }
        }
        files.resolutionStrategy.eachDependency(action)
      }
    })
  }
}
