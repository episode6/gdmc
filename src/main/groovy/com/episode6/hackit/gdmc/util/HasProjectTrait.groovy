package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.plugin.GdmcRootPlugin
import groovy.transform.Memoized
import org.gradle.api.Project
import org.gradle.api.tasks.Internal

/**
 * common code for objects that have an instance of project
 */
trait HasProjectTrait {

  @Internal
  abstract Project getProject()

  @Memoized @Internal
  DependencyMap getDependencyMap() {
    project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap
  }
}
