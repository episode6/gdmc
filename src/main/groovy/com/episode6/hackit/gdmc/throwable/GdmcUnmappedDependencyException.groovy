package com.episode6.hackit.gdmc.throwable

import com.episode6.hackit.gdmc.json.GdmcDependency

/**
 * Exception thrown when trying to build with an un-mapped dependency
 */
class GdmcUnmappedDependencyException extends RuntimeException {

  GdmcDependency unmappedDependency

  GdmcUnmappedDependencyException(GdmcDependency unmappedDependency) {
    super("Unmapped dependency found: ${unmappedDependency}")
  }
}
