package com.episode6.hackit.gdmc.json

import com.episode6.hackit.groovykit.versions.VersionComparator
import org.gradle.api.Nullable
import org.gradle.api.artifacts.ModuleVersionIdentifier

/**
 * dependency object as represented in json
 */
class GdmcDependency {

  static @Nullable GdmcDependency from(Object obj) {
    if (obj instanceof ModuleVersionIdentifier) {
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
      return null
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

  String getKey() {
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
}
