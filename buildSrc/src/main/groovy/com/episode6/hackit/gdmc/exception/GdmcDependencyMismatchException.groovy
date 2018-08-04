package com.episode6.hackit.gdmc.exception

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.GradleException

/**
 * Exception thrown from the {@link com.episode6.hackit.gdmc.task.GdmcValidateDepsTask} task when a
 * dependency is found that does not match its mapped dependency version in gdmc
 */
class GdmcDependencyMismatchException extends GradleException {

  private static String formatMessage(Map<GdmcDependency, String> errors) {
    StringBuilder msg = new StringBuilder("Dependency Mapping Errors: \n")
    errors.each { GdmcDependency dep, String reason ->
      msg.append("Mismatched dependency: ").append(dep.fullMavenKey).append(", reason: ").append(reason).append("\n")
    }
    return msg.toString()
  }

  GdmcDependencyMismatchException(Map<GdmcDependency, String> errors) {
    super(formatMessage(errors))
  }
}
