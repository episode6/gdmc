package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency
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
    println "found: ${details.requested.toString()}"
    GdmcDependency unMapped = GdmcDependency.from(details.requested)
    if (!unMapped.version) {
      apply(details, unMapped.getKey())
    }
//
//    if (details.requested.group == "com.episode6.hackit.gmdc_placeholder") {
//      String key = details.requested.toString().substring("com.episode6.hackit.gmdc_placeholder:".length())
//      apply(details, key)
//    } else if (!details.requested.version) {
//      apply(details, details.requested.toString())
//    }
  }

  private void apply(DependencyResolveDetails details, String key) {
    println "looking up key: ${key}"
    List<GdmcDependency> newTarget = dependencyMap.lookup(key)
    println "using target: ${newTarget}"
    if (!newTarget) {
      throw new RuntimeException("PUT A REAL EXCEPTION HERE")
    }
    details.useTarget(newTarget[0].toString())
    if (newTarget.size() > 1) {
      for (int i = 1; i < newTarget.length; i++) {
        configuration.dependencies.add(project.dependencies.create(newTarget[i].toString()))
      }
    }

  }
}
