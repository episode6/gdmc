package com.episode6.hackit.gdmc.exception

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.GradleException

/**
 * An exception thrown on a call to gdmcVersion when either too many or not enough mappings
 * are found for the given key.
 */
class GdmcVersionLookupException extends GradleException {
  private static String getMessage(Object key, List<GdmcDependency> mappedDeps) {
    if (mappedDeps.size() > 1) {
      return "Call to gdmcVersion(${key}) failed, too many dependencies found: ${mappedDeps}"
    }
    return "Call to gdmcVersion(${key}) failed, key not found"
  }

  GdmcVersionLookupException(Object key, List<GdmcDependency> mappedDeps) {
    super(getMessage(key, mappedDeps))
  }
}
