package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 *
 */
class DependencyMapImpl implements DependencyMap {
  private File gdmcFile
  private Map<String, GdmcDependency> mappedDependencies

  DependencyMapImpl(File file) {
    gdmcFile = file
    mappedDependencies = new LinkedHashMap<>()
    applyFile(gdmcFile, false)
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

  void applyFile(File file, boolean persist = true) {
    if (!file.exists()) {
      return
    }
    def json = new JsonSlurper().parse(file)
    if (json instanceof Map) {
      json.each { String key, value ->
        mappedDependencies.put(key, GdmcDependency.from(value))
      }
    } else if (json instanceof List) {
      json.each { value ->
        GdmcDependency dep = GdmcDependency.from(value)
        mappedDependencies.put(dep.key, dep)
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



//  void applyMissingDependencies(Set<GdmcDependency> newDependencies) {
//    applyDependencies(newDependencies.findAll({
//      return mappedDependencies.get(it.key) == null
//    }))
//  }
//
//  void applyUpgradedDependencies(Set<GdmcDependency> newDependencies) {
//    applyDependencies(newDependencies.findAll({
//      def mapped = mappedDependencies.get(it.key)
//      return !mapped || mapped.isOlderThan(it)
//    }))
//  }
//
//  void applyDependencies(Set<GdmcDependency> newDependencies) {
//    if (!newDependencies) {
//      return;
//    }
//    mappedDependencies.putAll(newDependencies.collectEntries {
//      return [(it.key): it]
//    })
//
//    Map sortedMap = mappedDependencies.collectEntries(new TreeMap(), { key, value ->
//      return [(key): value.toMap()]
//    })
//    gdmcFile.text = new JsonBuilder(sortedMap).toPrettyString()
//  }

}
