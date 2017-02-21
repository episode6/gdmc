package com.episode6.hackit.gdmc.data

import com.episode6.hackit.groovykit.versions.VersionComparator
import org.gradle.api.Nullable
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ModuleVersionSelector

/**
 * Dependency utility object. can represent either a resolved or unresolved dependency
 */
class GdmcDependency implements Serializable {

  private static final String PLACEHOLDER_GROUP_ID = "com.episode6.hackit.gmdc_placeholder"

  static @Nullable GdmcDependency from(Object obj) {
    if (obj instanceof ModuleVersionIdentifier || obj instanceof ModuleVersionSelector || obj instanceof Dependency) {
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
          alias: obj.get("alias"))
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

  boolean isPlaceholder() {
    return groupId == PLACEHOLDER_GROUP_ID
  }

  String getKey() {
    if (isPlaceholder()) {
      return artifactId
    }
    return "${groupId}:${artifactId}"
  }

  @Override
  String toString() {
    if (alias) {
      return alias.toString()
    }
    if (version) {
      return "${getKey()}:${version}"
    }
    return getKey()
  }

  String getPlaceholderKey() {
    if (isPlaceholder()) {
      return "${groupId}:${artifactId}"
    }
    return toString()
  }

  Map toMap() {
    if (alias) {
      return new LinkedHashMap(alias: alias)
    }
    return new LinkedHashMap(
        groupId: groupId,
        artifactId: artifactId,
        version: version)
  }

  boolean isOlderThan(GdmcDependency otherDependency) {
    return isOlderThanVersion(otherDependency?.version)
  }

  boolean isOlderThanVersion(String otherVersion) {
    if (!version) {
      return true
    }
    if (!otherVersion) {
      return false
    }
    return new VersionComparator().compare(version, otherVersion) < 0
  }

  GdmcDependency withoutVersion() {
    if (alias) {
      throw new IllegalAccessException("Called GdmcDependency.withoutVersion() on an alias: ${this}")
    }

    if (!version) {
      return this
    }

    return new GdmcDependency(
        groupId: groupId,
        artifactId: artifactId)
  }
}
