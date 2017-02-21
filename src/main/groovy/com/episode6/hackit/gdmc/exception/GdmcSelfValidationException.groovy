package com.episode6.hackit.gdmc.exception

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.GradleException

/**
 * Exception thrown from gdmcValidateSelf task
 */
class GdmcSelfValidationException extends GradleException {

  GdmcSelfValidationException(GdmcDependency selfDependency, List<GdmcDependency> mappedDependencies) {
    super("Failed to validate project ${selfDependency} in gdmc. Found ${mappedDependencies} instead")
  }
}
