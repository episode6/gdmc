package com.episode6.hackit.gdmc.util

import org.gradle.api.Project

/**
 * Gradle convention that enables the `gdmc` method at the project level
 */
class GdmcConvention implements HasProjectTrait {

  Project project

  def gdmc(Object key) {
    def mappedDeps = dependencyMap.lookupWithOverrides(key)
    return mappedDeps.collect {it.mapKey}
  }
}
