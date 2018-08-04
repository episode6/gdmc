package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.GdmcDependency

/**
 * Utility class for dealing with dependency keys
 */
class DependencyKeys {

  static String sanitize(Object obj) {
    if (obj instanceof Map) {
      String key = "${obj.group}:${obj.name}"
      if (obj.version) {
        return "${key}:${obj.version}"
      }
      return key
    }
    return obj
  }

  static GdmcDependency sanitizedGdmcDep(Object obj) {
    return GdmcDependency.from(sanitize(obj))
  }
}
