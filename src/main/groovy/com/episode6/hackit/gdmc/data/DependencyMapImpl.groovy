package com.episode6.hackit.gdmc.data

import com.episode6.hackit.gdmc.util.DependencyKeys
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * An implementation of DependencyMap. Currently this is backed by an in-memory map
 * built with jsonSluper. At some point I'd like to update this to a streaming json
 * parser so that it can scale.
 */
class DependencyMapImpl implements DependencyMap {
  private File gdmcFile
  private Map<String, GdmcDependency> mappedDependencies

  DependencyMapImpl(File file) {
    gdmcFile = file
    mappedDependencies = new LinkedHashMap<>()
    applyFile(gdmcFile, null, false)
  }

  @Override
  boolean isAlias(Object key) {
    String keyStr = removeTrailingColon(DependencyKeys.sanitize(key))
    def value = mappedDependencies.get(keyStr)
    return value?.alias
  }

  boolean isLocked(Object key) {
    String keyStr = removeTrailingColon(DependencyKeys.sanitize(key))
    def value = mappedDependencies.get(keyStr)
    return value?.locked
  }

  List<GdmcDependency> lookup(Object key) {
    return lookupKey(DependencyKeys.sanitize(key))
  }

  List<GdmcDependency> getValidDependencies() {
    return mappedDependencies.values().findAll {!it.alias}
  }

  void applyFile(File file, DependencyMap.DependencyFilter filter = null, boolean persist = true) {
    if (!file.exists()) {
      return
    }

    GChop.d("Applying file to dependency map: %s", file.absolutePath)

    def json = new JsonSlurper().parse(file)
    if (json instanceof Map) {
      json.each { String key, value ->
        GdmcDependency dep = GdmcDependency.from(value)
        if (!filter || filter.shouldApply(key, dep)) {
          mappedDependencies.put(key, dep)
        }
      }
    } else if (json instanceof List) {
      json.each { value ->
        GdmcDependency dep = GdmcDependency.from(value)
        if (!filter || filter.shouldApply(dep.key, dep)) {
          mappedDependencies.put(dep.key, dep)
        }
      }
    }

    if (persist) {
      writeToFile()
    }
  }

  void put(GdmcDependency dependency) {
    mappedDependencies.put(dependency.key, dependency)
    writeToFile()
  }

  private void writeToFile() {
    Map sortedMap = mappedDependencies.collectEntries(new TreeMap(), { key, value ->
      return [(key): value.toMap()]
    })
    gdmcFile.text = new JsonBuilder(sortedMap).toPrettyString()
  }

  private List<GdmcDependency> lookupKey(String key) {
    key = removeTrailingColon(key)
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

  private static String removeTrailingColon(String key) {
    if (key.endsWith(":")) {
      return key.substring(0, key.length()-1)
    }
    return key
  }
}
