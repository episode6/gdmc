package com.episode6.hackit.gdmc.util

import org.gradle.api.Project

/**
 * Accessor for project properties
 */
class ProjectProperties {

  private static final String OVERWRITE = "gdmc.overwrite"

  static boolean overwrite(Project project) {
    return project.hasProperty(OVERWRITE) && project.property(OVERWRITE)
  }
}
