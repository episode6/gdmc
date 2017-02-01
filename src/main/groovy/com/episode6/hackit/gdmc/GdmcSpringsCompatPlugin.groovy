package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.task.GdmcTasksPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * plugin to make gdmc compatible with Spring's dependency management plugin
 * https://github.com/spring-gradle-plugins/dependency-management-plugin
 */
class GdmcSpringsCompatPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    DependencyMap dependencyMap = GdmcRootPlugin.ensureInit(project).dependencyMap
    project.plugins.apply(GdmcTasksPlugin)

    project.afterEvaluate {
      project.dependencyManagement {
        dependencies {
          dependencyMap.validDependencies.each {
            dependency it.toString()
          }
        }
      }
    }
  }
}
