package com.episode6.hackit.gdmc.util

import groovy.transform.Memoized
import org.gradle.api.Project

/**
 * Accessor for project properties
 */
class ProjectProperties {

  private static final String PROP_OVERWRITE = "gdmc.overwrite"
  private static final String PROP_GDMC_FILE = "gdmc.file"

  private static final DEFAULT_FOLDER_NAME = "gdmc"
  private static final DEFAULT_FILE_NAME = "gdmc.json"

  static boolean overwrite(Project project) {
    return project.hasProperty(PROP_OVERWRITE) && project.property(PROP_OVERWRITE)
  }

  @Memoized
  static File gdmcFile(Project project) {
    if (project.hasProperty(PROP_GDMC_FILE)) {
      return new File(project.rootDir, project.property(PROP_GDMC_FILE) as String)
    }

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
