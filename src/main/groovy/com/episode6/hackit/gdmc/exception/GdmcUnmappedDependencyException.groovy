package com.episode6.hackit.gdmc.exception

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.GradleException

/**
 * Exception thrown when trying to build with an un-mapped dependency
 */
class GdmcUnmappedDependencyException extends GradleException {

  GdmcDependency unmappedDependency

  GdmcUnmappedDependencyException(GdmcDependency unmappedDependency) {
    super("Unmapped dependency found: ${unmappedDependency}")
    this.unmappedDependency = unmappedDependency
  }
}
