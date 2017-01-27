package com.episode6.hackit.gdmc

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
    if (details.requested.group == "com.episode6.hackit.gmdc_placeholder") {
      String key = details.requested.toString().substring("com.episode6.hackit.chop:chop-android".length())
      if (key.endsWith(":")) {
        key = key.substring(0, key.length()-1)
      }
      println "looking up key: ${key}"
      def newTarget = dependencyMap.lookup(key)
      println "found new target: ${newTarget}"
      if (newTarget instanceof String[]) {
        details.useTarget(newTarget[0])
        for (int i = 1; i < newTarget.length; i++) {
          configuration.dependencies.add(project.dependencies.create(newTarget[i]))
        }
      } else {
        details.useTarget(newTarget)
      }
      return
    }

    if (!details.requested.version) {
      details.useTarget(dependencyMap.lookup("${details.requested.group}:${details.requested.name}"))
    }
  }
}
