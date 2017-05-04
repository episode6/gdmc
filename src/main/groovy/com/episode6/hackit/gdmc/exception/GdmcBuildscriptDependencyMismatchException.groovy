package com.episode6.hackit.gdmc.exception

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.GradleException

/**
 * Exception thrown from the gdmcValidateBuildscriptDeps task when a buildscript dependency is found that
 * does not match its mapped dependency version in gdmc
 */
class GdmcBuildscriptDependencyMismatchException extends GradleException {

  private static String formatMessage(Map<GdmcDependency, String> errors) {
    StringBuilder msg = new StringBuilder("Buildscript Dependency Mapping Errors: \n")
    errors.each { GdmcDependency dep, String reason ->
      msg.append("Mismatched dependency: ").append(dep).append(", reason: ").append(reason).append("\n")
    }
    return msg.toString()
  }

  GdmcBuildscriptDependencyMismatchException(Map<GdmcDependency, String> errors) {
    super(formatMessage(errors))
  }
}
