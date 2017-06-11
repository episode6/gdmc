package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.exception.GdmcVersionLookupException
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

  def gdmcVersion(Object key) {
    def mappedDeps = dependencyMap.lookupWithOverrides(key)
    if (mappedDeps.size() != 1) {
      throw new GdmcVersionLookupException(key, mappedDeps)
    }
    return mappedDeps.get(0).version
  }
}
