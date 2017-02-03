package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.DependencyMapImpl
import com.episode6.hackit.gdmc.data.GdmcDependency
import groovy.transform.Memoized
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for root projects only, holds the single instance of the dependency map
 */
class GdmcRootPlugin implements Plugin<Project> {

  public static GdmcRootPlugin ensureInit(Project project) {
    GdmcRootPlugin rootPlugin = project.rootProject.plugins.findPlugin(GdmcRootPlugin)
    if (!rootPlugin) {
      rootPlugin = project.rootProject.plugins.apply(GdmcRootPlugin)
    }
    return rootPlugin
  }

  static final DEFAULT_FOLDER_NAME = "gdmc"
  static final DEFAULT_FILE_NAME = "gdmc.json"

  Project project
  DependencyMap dependencyMap

  @Override
  void apply(Project project) {
    if (project != project.rootProject) {
      throw new IllegalArgumentException("GdmcRootPlugin must only be applied to the root of the project")
    }

    this.project = project
    dependencyMap = new DependencyMapImpl(gdmcFile)

    Project.metaClass.gdmc = { key ->
      DependencyMap depMap = rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap
      List<GdmcDependency> deps = depMap.lookup(key)
      if (!deps) {
        GdmcDependency rawDep = GdmcDependency.from(depMap.sanitizeKey(key))
        return rawDep.getPlaceholderKey()
      }
      return deps.collect {
        it.toString()
      }

    }
  }

  @Memoized
  File getGdmcFile() {
    File defaultFile = new File(project.rootDir, DEFAULT_FILE_NAME)
    if (defaultFile.exists()) {
      return defaultFile
    }

    File gdmcFolder = new File(project.rootDir, DEFAULT_FOLDER_NAME)
    if (!gdmcFolder.exists() || !gdmcFolder.isDirectory()) {
      return defaultFile
    }

    File gdmcSubFile = new File(gdmcFolder, DEFAULT_FILE_NAME)
    if (gdmcSubFile.exists()) {
      return gdmcSubFile
    }

    return defaultFile
  }
}
