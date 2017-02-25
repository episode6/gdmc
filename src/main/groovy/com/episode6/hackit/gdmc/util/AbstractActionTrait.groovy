package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.plugin.GdmcRootPlugin
import groovy.transform.Memoized
import org.gradle.api.Project

/**
 * common code shared by actions
 */
trait AbstractActionTrait {
  Project project

  @Memoized
  DependencyMap getDependencyMap() {
    project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap
  }
}
