package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ModuleVersionSelector

/**
 * Wrapper class so we can re-use the logic in VersionMapperAction for maven mappings as well
 */
class DependencyResolveDetailsWrapper implements DependencyResolveDetails {

  private Object dependencyDelegate
  private ModuleVersionSelector moduleVersionSelector

  DependencyResolveDetailsWrapper(Object dependencyDelegate) {
    this.dependencyDelegate = dependencyDelegate
    this.moduleVersionSelector = new ModuleVersionSelectorWrapper()
  }

  @Override
  ModuleVersionSelector getRequested() {
    return moduleVersionSelector
  }

  @Override
  void useVersion(String s) {
    dependencyDelegate.version = s
  }

  @Override
  void useTarget(Object o) {
    GdmcDependency dependency = DependencyKeys.sanitizedGdmcDep(o)
    dependencyDelegate.groupId = dependency.groupId
    dependencyDelegate.artifactId = dependency.artifactId
    dependencyDelegate.version = dependency.version
  }

  @Override
  ModuleVersionSelector getTarget() {
    return null
  }

  @Override
  String toString() {
    return moduleVersionSelector.toString()
  }

  class ModuleVersionSelectorWrapper implements ModuleVersionSelector {

    @Override
    String getGroup() {
      return dependencyDelegate.groupId
    }

    @Override
    String getName() {
      return dependencyDelegate.artifactId
    }

    @Override
    String getVersion() {
      return dependencyDelegate.version
    }

    @Override
    boolean matchesStrictly(ModuleVersionIdentifier moduleVersionIdentifier) {
      return group == moduleVersionIdentifier.group &&
          name == moduleVersionIdentifier.name &&
          version == moduleVersionIdentifier.version
    }

    @Override
    String toString() {
      return "${group}:${name}:${version}"
    }
  }
}
