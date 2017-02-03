package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.exception.GdmcUnmappedDependencyException
import com.episode6.hackit.gdmc.plugin.GdmcRootPlugin
import groovy.transform.Memoized
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

/**
 * Resolves missing versions from the provided
 */
class VersionMapperAction implements Action<DependencyResolveDetails> {

  Configuration configuration
  Project project

  @Override
  void execute(DependencyResolveDetails details) {
    GdmcLogger.Chop.d("Attempting to resolve %s", details.requested)
    GdmcDependency unMapped = GdmcDependency.from(details.requested)
    if (unMapped.version) {
      GdmcLogger.Chop.d("%s has a version, skipping", details.requested)
      return
    }

    List<String> mappedDeps = dependencyMap.lookup(unMapped.key).collect {it.toString()}
    if (!mappedDeps) {
      throw GdmcLogger.Chop.e(
          new GdmcUnmappedDependencyException(unMapped),
          "Could not find mapped dependency for key: %s",
          unMapped.key)
    }

    GdmcLogger.Chop.d("Replacing %s with %s", details.requested, mappedDeps[0])
    details.useTarget(mappedDeps[0])
    if (mappedDeps.size() > 1) {
      for (int i = 1; i < mappedDeps.size(); i++) {
        GdmcLogger.Chop.d("Adding extra dependency %s to config %s", mappedDeps[i], configuration.name)
        configuration.dependencies.add(project.dependencies.create(mappedDeps[i]))
      }
    }
  }

  @Memoized
  DependencyMap getDependencyMap() {
    project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap
  }
}
