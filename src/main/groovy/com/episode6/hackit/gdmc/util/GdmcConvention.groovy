package com.episode6.hackit.gdmc.util

/**
 * Gradle convention that enables the `gdmc` method at the project level
 */
class GdmcConvention {
  def gdmc(Object key) {
    return DependencyKeys.sanitizedGdmcDep(key).placeholderKey
  }
}
