package com.episode6.hackit.gdmc.exception

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.GradleException

/**
 * Exception thrown from the gdmcValidateBuildscriptDeps task when a buildscript dependency is found that
 * does not match its mapped dependency version in gdmc
 */
class GdmcBuildscriptDependencyMismatchException extends GradleException {

  GdmcBuildscriptDependencyMismatchException(GdmcDependency offendingDependency, String mappedVersion) {
    super("Buildscript Dependency Version Mismatch - offending dependency: ${offendingDependency}, mapped version: ${mappedVersion}")
  }

  GdmcBuildscriptDependencyMismatchException(GdmcDependency offendingDependency, List<GdmcDependency> mappedDeps) {
    super("Buildscript Dependency Mapping Error - dependency appears to be an alias - offending dependency: ${offendingDependency}, alias to: ${mappedDeps}")
  }
}
