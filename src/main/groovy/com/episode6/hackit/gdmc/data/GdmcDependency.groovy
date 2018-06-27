package com.episode6.hackit.gdmc.data

import groovy.transform.EqualsAndHashCode
import org.gradle.api.GradleException
import org.gradle.api.Nullable
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ModuleVersionSelector

import static com.episode6.hackit.gdmc.util.Assertions.assertOnlyOne

/**
 * Dependency utility object. can represent either a resolved or unresolved dependency
 */
@EqualsAndHashCode
class GdmcDependency implements Serializable {
  static @Nullable GdmcDependency fromMap(DependencyMap dependencyMap, Map obj, String mapKey = null) {
    return new DepFromDependencyMap(
        groupId: obj.get("groupId"),
        artifactId: obj.get("artifactId"),
        version: obj.get("version"),
        inheritedVersionFrom: obj.get("inheritVersion"),
        alias: obj.get("alias"),
        locked: obj.get("locked"),
        mapKey: mapKey,
        dependencyMap: dependencyMap)
  }

  static @Nullable GdmcDependency from(Object obj) {
    if (obj instanceof ModuleVersionIdentifier || obj instanceof ModuleVersionSelector || obj instanceof Dependency || obj instanceof Project) {
      return new GdmcDependency(
          groupId: obj.group,
          artifactId: obj.name,
          version: obj.version,
          mapKey: null)
    }
    return fromString(obj.toString())
  }

  static @Nullable GdmcDependency fromString(String identifier, String mapKey = null) {
    String[] tokens = identifier.tokenize(":")
    if (tokens.length < 2 || tokens.length > 3) {
      throw new GradleException("Unexpected number of tokens in dependency identifier: ${identifier}")
    }
    return new GdmcDependency(
        groupId: tokens[0],
        artifactId: tokens[1],
        version: tokens.length > 2 ? tokens[2] : null,
        mapKey: mapKey)
  }

  Object alias
  String groupId
  String artifactId
  String version
  String inheritedVersionFrom
  Boolean locked
  String mapKey

  boolean matches(GdmcDependency otherDep) {
    return getFullMavenKey().equals(otherDep.getFullMavenKey())
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
    return "${groupId}:${artifactId}"
  }

  String getFullMavenKey() {
    if (alias) {
      throw new GradleException("called getFullMavenKey on alias: ${this}")
    }
    if (getVersion()) {
      return "${getMavenKey()}:${getVersion()}"
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
    LinkedHashMap map = new LinkedHashMap();
    if (groupId) {
      map.put("groupId", groupId)
    }
    if (artifactId) {
      map.put("artifactId", artifactId)
    }

    if (inheritedVersionFrom) {
      map.put("inheritVersion", inheritedVersionFrom)
    } else if (getVersion()) {
      map.put("version", getVersion())
    }

    if (locked) {
      map.put("locked", locked)
    }
    return map
  }

  GdmcDependency withoutVersion() {
    if (alias) {
      throw new GradleException("Called GdmcDependency.withoutVersion() on an alias: ${this}")
    }
    if (!getVersion()) {
      return this
    }
    return new GdmcDependency(
        groupId: groupId,
        artifactId: artifactId,
        mapKey: mapKey)
  }

  boolean matchesAnyProject(Project project) {
    project.rootProject.allprojects.find {
      groupId == it.group && artifactId == it.name
    } != null
  }

  private static class DepFromDependencyMap extends GdmcDependency implements Serializable {

    transient DependencyMap dependencyMap

    @Override
    String getVersion() {
      if (inheritedVersionFrom) {
        try {
          return assertOnlyOne(
              dependencyMap.lookupWithOverrides(inheritedVersionFrom)).getVersion()
        } catch (GradleException e) {
          throw new GradleException("Lookup failed for inherited version on key ${getMapKey()}", e)
        }
      }
      return super.getVersion()
    }

    @Override
    Boolean getLocked() {
      if (inheritedVersionFrom) {
        return true
      }
      return super.getLocked()
    }
  }
}
