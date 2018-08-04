package com.episode6.hackit.gdmc.util

import org.gradle.api.GradleException

/**
 *
 */
class Assertions {
  static <T> T assertOnlyOne(Collection<T> collection) {
    if (collection.isEmpty()) {
      throw new GradleException("Expected exactly on entry, found 0")
    }
    if (collection.size() > 1) {
      throw new GradleException("Expected exactly on entry, found ${collection.size()}")
    }
    return collection.first()
  }
}
