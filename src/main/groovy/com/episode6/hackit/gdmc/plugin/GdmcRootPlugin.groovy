package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.DependencyMapImpl
import com.episode6.hackit.gdmc.util.DependencyKeys
import groovy.transform.Memoized
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
      return DependencyKeys.sanitizedGdmcDep(key).placeholderKey
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
