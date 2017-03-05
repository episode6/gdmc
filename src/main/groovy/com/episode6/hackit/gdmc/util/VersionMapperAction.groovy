package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.exception.GdmcUnmappedDependencyException
import groovy.transform.Memoized
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolveDetails

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Resolves missing versions from the provided DependencyResolveDetails
 */
class VersionMapperAction implements Action<DependencyResolveDetails>, HasProjectTrait {

  Project project

  VersionMapperAction(Map opts) {
    this.project = opts.project
  }

  /**
   * This method is seperate so that it can be overridden in the SpringsCompat plugin.
   * Return true to skip this dependency (i.e. leave it as is) or false to look it up
   * and replace it.
   */
  boolean shouldSkipMappingVersion(GdmcDependency unMapped) {
    return unMapped.version
  }

  @Override
  void execute(DependencyResolveDetails details) {
    GChop.d("Attempting to resolve %s", details.requested)
    GdmcDependency unMapped = GdmcDependency.from(details.requested)
    if (shouldSkipMappingVersion(unMapped)) {
      GChop.d("Skipping mapping for %s", details.requested)
      return
    }

    List<String> mappedDeps = dependencyMap.lookup(unMapped.key).collect {it.toString()}
    if (!mappedDeps) {
      handleUnmappedDependency(details, unMapped)
      return;
    }

    GChop.d("Replacing %s with %s", details.requested, mappedDeps[0])
    details.useTarget(mappedDeps[0])
    // if mappedDeps.size() > 1, the other deps will have been added prior to this action being run
  }

  private void handleUnmappedDependency(DependencyResolveDetails details, GdmcDependency unMapped) {
    if (forceResolve()) {
      GChop.e("Unmapped dependency found: %s, forceResolve flag set, using + as version")
      details.useVersion("+")
    } else {
      throw GChop.e(
          new GdmcUnmappedDependencyException(unMapped),
          "Could not find mapped dependency for key: %s",
          unMapped.key)
    }
  }

  @Memoized
  boolean forceResolve() {
    return ProjectProperties.forceResolve(project)
  }
}
