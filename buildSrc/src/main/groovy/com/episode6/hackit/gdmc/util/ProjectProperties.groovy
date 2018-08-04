package com.episode6.hackit.gdmc.util

import groovy.transform.Memoized
import org.gradle.api.Project
import org.gradle.api.plugins.MavenPlugin
import org.gradle.api.publish.ivy.plugins.IvyPublishPlugin
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

/**
 * Accessor for project properties
 */
class ProjectProperties {

  private static final String PROP_OVERWRITE = "gdmc.overwrite"
  private static final String PROP_GDMC_FILE = "gdmc.file"
  private static final String PROP_GDMC_OVERRIDE_FILES = "gdmc.overrideFiles"
  private static final String PROP_FORCE_RESOLVE = "gdmc.forceResolve"

  private static final DEFAULT_FOLDER_NAME = "gdmc"
  private static final DEFAULT_FILE_NAME = "gdmc.json"

  static boolean overwrite(Project project) {
    return booleanProperty(project, PROP_OVERWRITE)
  }

  static boolean forceResolve(Project project) {
    return booleanProperty(project, PROP_FORCE_RESOLVE)
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

  @Memoized
  static List<File> overrideFiles(Project project) {
    if (!project.hasProperty(PROP_GDMC_OVERRIDE_FILES)) {
      return []
    }

    def overrideFiles = project.property(PROP_GDMC_OVERRIDE_FILES) as String
    overrideFiles.tokenize("|").collect {
      return new File(project.rootDir, it)
    }
  }

  static boolean isProjectDeployable(Project project) {
    def plugins = project.plugins
    return plugins.findPlugin(MavenPlugin) != null ||
        plugins.findPlugin(MavenPublishPlugin) != null ||
        plugins.findPlugin(IvyPublishPlugin) != null ||
        plugins.findPlugin("com.jfrog.bintray") != null
  }

  private static boolean booleanProperty(Project project, String propName) {
    return project.hasProperty(propName) && project.property(propName)
  }
}
