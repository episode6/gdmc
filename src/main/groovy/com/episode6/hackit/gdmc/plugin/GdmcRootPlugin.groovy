package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.DependencyMapImpl
import com.episode6.hackit.gdmc.util.DependencyKeys
import com.episode6.hackit.gdmc.util.ProjectProperties
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for root projects only, holds the single instance of the dependency
 * map and adds the gdmc method to Project.
 */
class GdmcRootPlugin implements Plugin<Project> {

  public static GdmcRootPlugin ensureInit(Project project) {
    GdmcRootPlugin rootPlugin = project.rootProject.plugins.findPlugin(GdmcRootPlugin)
    if (!rootPlugin) {
      rootPlugin = project.rootProject.plugins.apply(GdmcRootPlugin)
    }
    return rootPlugin
  }

  Project project
  DependencyMap dependencyMap

  @Override
  void apply(Project project) {
    if (project != project.rootProject) {
      throw new IllegalArgumentException("GdmcRootPlugin must only be applied to the root of the project")
    }

    this.project = project
    dependencyMap = new DependencyMapImpl(ProjectProperties.gdmcFile(project))
    ProjectProperties.overrideFiles(project).each {
      dependencyMap.applyOverrides(it)
    }
  }
}
