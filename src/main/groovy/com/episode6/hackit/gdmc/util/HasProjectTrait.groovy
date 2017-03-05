package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.plugin.GdmcRootPlugin
import groovy.transform.Memoized
import org.gradle.api.Project

/**
 * common code for objects that have an instance of project
 */
trait HasProjectTrait {

  abstract Project getProject()

  @Memoized
  DependencyMap getDependencyMap() {
    project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap
  }
}
