package com.episode6.hackit.gdmc

import groovy.transform.Memoized
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for root projects only
 */
class GdmcRootProjectPlugin implements Plugin<Project> {

  static final DEFAULT_FOLDER_NAME = "gdmc"
  static final DEFAULT_FILE_NAME = "gdmc.json"

  Project project
  GdmcDependencyContainer dependencies

  @Override
  void apply(Project project) {
    if (project != project.rootProject) {
      throw new IllegalArgumentException("GdmcRootProjectPlugin must only be applied to the root of the project")
    }

    this.project = project
    dependencies = new GdmcDependencyContainer()
    if (gdmcFile.exists()) {
      println "applying file ${gdmcFile.absolutePath}"
      dependencies.applyFile(gdmcFile)
    }

    Project.metaClass.gdmc = { key ->
      return rootProject.plugins.getPlugin(GdmcRootProjectPlugin).dependencies.lookup(key)
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
