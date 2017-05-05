package com.episode6.hackit.gdmc.data

import groovy.transform.EqualsAndHashCode
import org.gradle.api.GradleException
import org.gradle.api.Nullable
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ModuleVersionSelector

/**
 * Dependency utility object. can represent either a resolved or unresolved dependency
 */
@EqualsAndHashCode
class GdmcDependency implements Serializable {

  private static final String PLACEHOLDER_GROUP_ID = "com.episode6.hackit.gmdc_placeholder"

  static @Nullable GdmcDependency from(Object obj) {
    if (obj instanceof ModuleVersionIdentifier || obj instanceof ModuleVersionSelector || obj instanceof Dependency || obj instanceof Project) {
      return new GdmcDependency(
          groupId: obj.group,
          artifactId: obj.name,
          version: obj.version)
    }
    if (obj instanceof Map) {
      return new GdmcDependency(
          groupId: obj.get("groupId"),
          artifactId: obj.get("artifactId"),
          version: obj.get("version"),
          alias: obj.get("alias"),
          locked: obj.get("locked"))
    }
    return fromString(obj)
  }

  static @Nullable GdmcDependency fromString(String identifier) {
    String[] tokens = identifier.tokenize(":")
    if (tokens.length < 2 || tokens.length > 3) {
      return new GdmcDependency(
          groupId: PLACEHOLDER_GROUP_ID,
          artifactId: identifier)
    }
    return new GdmcDependency(
        groupId: tokens[0],
        artifactId: tokens[1],
        version: tokens.length > 2 ? tokens[2] : null)
  }

  Object alias
  String groupId
  String artifactId
  String version
  Boolean locked
  String mapKey

  boolean isPlaceholder() {
    return groupId == PLACEHOLDER_GROUP_ID
  }

  boolean isMappedToMavenKey() {
    if (alias) {
      return false;
    }
    return getMapKey() == getMavenKey()
  }

  String getMapKey() {
    if (mapKey) {
      return mapKey
    }
    return getMavenKey()
  }

  String getMavenKey() {
    if (alias) {
      throw new GradleException("called getMavenKey on alias: ${this}")
    }
    if (isPlaceholder()) {
      return artifactId
    }
    return "${groupId}:${artifactId}"
  }

  String getFullMavenKey() {
    if (alias) {
      throw new GradleException("called getFullMavenKey on alias: ${this}")
    }
    if (version) {
      return "${getMavenKey()}:${version}"
    }
    return getMavenKey()
  }

  @Override
  String toString() {
    return "GdmcDependency{${alias ? "alias: ${alias}" : getFullMavenKey()}}"
  }

  Map toMap() {
    if (alias) {
      return new LinkedHashMap(alias: alias)
    }
    LinkedHashMap map = new LinkedHashMap(
        groupId: groupId,
        artifactId: artifactId,
        version: version)
    if (locked) {
      map.put("locked", locked)
    }
    return map
  }

  GdmcDependency withoutVersion() {
    if (alias) {
      throw new GradleException("Called GdmcDependency.withoutVersion() on an alias: ${this}")
    }
    if (!version) {
      return this
    }
    return new GdmcDependency(
        groupId: groupId,
        artifactId: artifactId)
  }

  boolean matchesAnyProject(Project project) {
    project.rootProject.allprojects.find {
      groupId == it.group && artifactId == it.name
    } != null
  }
}
