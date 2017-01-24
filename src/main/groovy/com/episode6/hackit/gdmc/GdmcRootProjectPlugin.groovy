package com.episode6.hackit.gdmc

import org.gradle.api.Nullable
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for root projects only
 */
class GdmcRootProjectPlugin implements Plugin<Project> {

  static final DEFAULT_FOLDER_NAME = "gdmc"
  static final DEFAULT_FILE_NAME = "gdmc.json"

  GdmcDependencyContainer dependencies
  File gdmcFile

  @Override
  void apply(Project project) {
    if (project != project.rootProject) {
      throw new IllegalArgumentException("GdmcRootProjectPlugin must only be applied to the root of the project")
    }

    dependencies = new GdmcDependencyContainer()
    gdmcFile = findGdmcFile(project.rootDir)
    if (gdmcFile != null) {
      println "applying file ${gdmcFile.absolutePath}"
      dependencies.applyFile(gdmcFile)
    }

    Project.metaClass.gdmc = { key ->
      return rootProject.plugins.getPlugin(GdmcRootProjectPlugin).dependencies.lookup(key)
    }
  }

  private @Nullable File findGdmcFile(File rootDir) {
    File gdmcFile = new File(rootDir, DEFAULT_FILE_NAME)
    if (gdmcFile.exists()) {
      println "root gdmc file exists, returning ${gdmcFile.absolutePath}"
      return gdmcFile
    }

    File gdmcFolder = new File(rootDir, DEFAULT_FOLDER_NAME)
    if (!gdmcFolder.exists() || !gdmcFolder.isDirectory()) {
      println "gdmc folder does not exist, returing null ${gdmcFolder.absolutePath}"
      return null
    }

    gdmcFile = new File(gdmcFolder, DEFAULT_FILE_NAME)
    println "final gdmc file exists: ${gdmcFile.exists()}, path: ${gdmcFile.absolutePath}"
    return gdmcFile.exists() ? gdmcFile : null
  }
}
