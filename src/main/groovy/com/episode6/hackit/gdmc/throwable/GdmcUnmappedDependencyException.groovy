package com.episode6.hackit.gdmc.throwable

import com.episode6.hackit.gdmc.json.GdmcDependency
import org.gradle.api.GradleException

/**
 * Exception thrown when trying to build with an un-mapped dependency
 */
class GdmcUnmappedDependencyException extends GradleException {

  GdmcDependency unmappedDependency

  GdmcUnmappedDependencyException(GdmcDependency unmappedDependency) {
    super("Unmapped dependency found: ${unmappedDependency}")
  }
}
