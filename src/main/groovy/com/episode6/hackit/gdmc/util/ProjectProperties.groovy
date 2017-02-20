package com.episode6.hackit.gdmc.util

import org.gradle.api.Project

/**
 * Accessor for project properties
 */
class ProjectProperties {

  static boolean overwrite(Project project) {
    return project.hasProperty("overwrite") && project.overwrite
  }
}
