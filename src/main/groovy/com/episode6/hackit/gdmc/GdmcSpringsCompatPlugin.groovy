package com.episode6.hackit.gdmc

import org.gradle.api.Plugin
import org.gradle.api.Project

import static GdmcLogger.Chop

/**
 * plugin to make gdmc compatible with Spring's dependency management plugin
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
          Chop.d("added dependency %s to dependncyManager", it)
          dependency it.toString()
        }
      }
    }
  }
}
