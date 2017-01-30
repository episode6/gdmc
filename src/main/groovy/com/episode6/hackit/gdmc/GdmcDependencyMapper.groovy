package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.Memoized
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for root projects only, holds the single instance of the dependency map
 */
class GdmcDependencyMapper implements Plugin<Project>, DependencyMap {

  static final DEFAULT_FOLDER_NAME = "gdmc"
  static final DEFAULT_FILE_NAME = "gdmc.json"

  Project project
  Map<String, GdmcDependency> mappedDependencies = new LinkedHashMap()
  List<String> missingDependencies = new LinkedList<>()

  @Override
  void apply(Project project) {
    if (project != project.rootProject) {
      throw new IllegalArgumentException("GdmcDependencyMapper must only be applied to the root of the project")
    }

    this.project = project
    if (gdmcFile.exists()) {
      new JsonSlurper().parse(gdmcFile).each { String key, value ->
        mappedDependencies.put(key, GdmcDependency.from(value))
      }
    }

    Project.metaClass.gdmc = { key ->
      return rootProject.plugins.getPlugin(GdmcDependencyMapper).lookup(key)
    }
    Project.metaClass.gdmc2 = { String key ->
      if (!key.contains(":")) {
        return "com.episode6.hackit.gmdc_placeholder:${key}"
      }
    }
  }

  @Memoized
  File getGdmcFile() {
    File defaultFile = new File(project.rootDir, DEFAULT_FILE_NAME)
    if (defaultFile.exists()) {
      return defaultFile
    }

    File gdmcFolder = new File(project.rootDir, DEFAULT_FOLDER_NAME)
    if (!gdmcFolder.exists() || !gdmcFolder.isDirectory()) {
      return defaultFile
    }

    File gdmcSubFile = new File(gdmcFolder, DEFAULT_FILE_NAME)
    if (gdmcSubFile.exists()) {
      return gdmcSubFile
    }

    return defaultFile
  }

  Object lookup(Object key) {
    if (key instanceof Map) {
      return lookupMap(key)
    }
    return lookupKey((String)key)
  }

  private Object lookupMap(Map params) {
    String key = "${params.group}:${params.name}"
    if (params.version) {
      key = "${key}:${params.version}"
    }
    return lookupKey(key)
  }

  private Object lookupKey(String key) {
    def value = mappedDependencies.get(key)
    if (value == null) {
      missingDependencies.add(key)
      return "${key}:+"
//      throw new RuntimeException("MISSING DEP: ${key} - PUT A REAL EXCEPTION HERE")
    }

    if (!value.alias) {
      return "${value.groupId}:${value.artifactId}:${value.version}"
    }

    if (value.alias instanceof List) {
      List<String> resolvedKeys = new ArrayList<>()
      value.alias.each { String it ->
        def resolved = lookupKey(it)
        if (resolved instanceof String[]) {
          resolvedKeys.addAll(resolved)
        } else {
          resolvedKeys.add((String)resolved)
        }
      }
      return (String[])resolvedKeys.toArray()
    }
    return lookupKey((String)value.alias)
  }

  void applyMissingDependencies(Set<GdmcDependency> newDependencies) {
    applyDependencies(newDependencies.findAll({
      return mappedDependencies.get(it.key) == null
    }))
  }

  void applyUpgradedDependencies(Set<GdmcDependency> newDependencies) {
    applyDependencies(newDependencies.findAll({
      def mapped = mappedDependencies.get(it.key)
      return !mapped || mapped.isOlderThan(it)
    }))
  }

  void applyDependencies(Set<GdmcDependency> newDependencies) {
    if (!newDependencies) {
      return;
    }
    mappedDependencies.putAll(newDependencies.collectEntries {
      return [(it.key): it]
    })

    Map sortedMap = mappedDependencies.collectEntries(new TreeMap(), { key, value ->
      return [(key): value.toMap()]
    })
    gdmcFile.text = new JsonBuilder(sortedMap).toPrettyString()
  }
}
