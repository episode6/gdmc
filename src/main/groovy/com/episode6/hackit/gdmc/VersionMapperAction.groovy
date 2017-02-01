package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency
import com.episode6.hackit.gdmc.throwable.GdmcUnmappedDependencyException
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyResolveDetails

/**
 * Resolves missing versions from the provided
 */
class VersionMapperAction implements Action<DependencyResolveDetails> {

  DependencyMap dependencyMap
  Configuration configuration
  Project project

  @Override
  void execute(DependencyResolveDetails details) {
    GdmcDependency unMapped = GdmcDependency.from(details.requested)
    if (unMapped.version) {
      return
    }

    List<String> mappedDeps = dependencyMap.lookup(unMapped.key).collect {it.toString()}
    if (!mappedDeps) {
      throw new GdmcUnmappedDependencyException(unMapped)
    }

    details.useTarget(mappedDeps[0])
    if (mappedDeps.size() > 1) {
      for (int i = 1; i < mappedDeps.size(); i++) {
        configuration.dependencies.add(project.dependencies.create(mappedDeps[i]))
      }
    }
  }
}
