package com.episode6.hackit.gdmc.data

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import static com.episode6.hackit.gdmc.util.GdmcLogger.Chop

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

  String sanitizeKey(Object obj) {
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
    return lookupKey(sanitizeKey(key))
  }

  List<GdmcDependency> getValidDependencies() {
    return mappedDependencies.values().findAll {!it.alias}
  }

  void applyFile(File file, DependencyMap.DependencyFilter filter = null, boolean persist = true) {
    if (!file.exists()) {
      return
    }

    Chop.d("Applying file to dependency map: %s", file.absolutePath)

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
      Map sortedMap = mappedDependencies.collectEntries(new TreeMap(), { key, value ->
        return [(key): value.toMap()]
      })
      gdmcFile.text = new JsonBuilder(sortedMap).toPrettyString()
    }
  }

  private List<GdmcDependency> lookupKey(String key) {
    if (key.endsWith(":")) {
      key = key.substring(0, key.length()-1)
    }
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
}
