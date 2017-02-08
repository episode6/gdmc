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

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Resolves missing versions from the provided DependencyResolveDetails
 */
class VersionMapperAction implements Action<DependencyResolveDetails> {

  Configuration configuration
  Project project

  @Override
  void execute(DependencyResolveDetails details) {
    GChop.d("Attempting to resolve %s", details.requested)
    GdmcDependency unMapped = GdmcDependency.from(details.requested)
    if (unMapped.version) {
      GChop.d("%s has a version, skipping", details.requested)
      return
    }

    List<String> mappedDeps = dependencyMap.lookup(unMapped.key).collect {it.toString()}
    if (!mappedDeps) {
      throw GChop.e(
          new GdmcUnmappedDependencyException(unMapped),
          "Could not find mapped dependency for key: %s",
          unMapped.key)
    }

    GChop.d("Replacing %s with %s", details.requested, mappedDeps[0])
    details.useTarget(mappedDeps[0])
    if (mappedDeps.size() > 1) {
      for (int i = 1; i < mappedDeps.size(); i++) {
        GChop.d("Adding extra dependency %s to config %s", mappedDeps[i], configuration.name)
        configuration.dependencies.add(project.dependencies.create(mappedDeps[i]))
      }
    }
  }

  @Memoized
  DependencyMap getDependencyMap() {
    project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap
  }
}
