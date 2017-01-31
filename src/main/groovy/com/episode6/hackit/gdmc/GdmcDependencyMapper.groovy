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
      List<GdmcDependency> deps = rootProject.plugins.getPlugin(GdmcDependencyMapper).lookup(key)
      if (!deps) {
        GdmcDependency rawDep = GdmcDependency.from(getKeyFromMap(key))
        return rawDep.getPlaceholderKey()
      }
      return deps.collect {
        it.toString()
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

  private String getKeyFromMap(Object obj) {
    if (obj instanceof Map) {
      String key = "${obj.group}:${obj.name}"
      if (obj.version) {
        return "${key}:${obj.version}"
      }
      return key
    }
    return obj
  }

  List<GdmcDependency> lookup(Object key) {
    if (key instanceof Map) {
      return lookupKey(getKeyFromMap(key))
    }
    return lookupKey((String)key)
  }

  private List<GdmcDependency> lookupKey(String key) {
    if (key.endsWith(":")) {
      key = key.substring(0, key.length()-1)
    }
    println "called lookup: ${key}"
    def value = mappedDependencies.get(key)
    if (value == null) {
      return []
    }

    if (!value.alias) {
      return [value]
    }

    if (value.alias instanceof List) {
      return value.alias.collectMany {
        lookupKey(it)
      }
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
