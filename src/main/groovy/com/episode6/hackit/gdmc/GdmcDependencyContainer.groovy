package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency
import com.episode6.hackit.gdmc.throwable.GdmcParseException
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * Container for the gdmc dependency hashmap
 */
class GdmcDependencyContainer {
  Map<String, GdmcDependency> map = new LinkedHashMap()
  List<String> missingDependencies = new LinkedList<>()

  void applyFile(File jsonFile) {
    try {
      Map jsonMap = new JsonSlurper().parse(jsonFile) as Map
      jsonMap.each { String key, value ->
        map.put(key, GdmcDependency.from(value))
      }
    } catch (Throwable t) {
      throw new GdmcParseException("Failed to apply gdmc file: ${jsonFile.absolutePath}", t)
    }
  }

  void applyChanges(Map<String, GdmcDependency> newDependencies) {
    map.putAll(newDependencies)
  }

  void writeToFile(File file) {
    Map toFileMap = new TreeMap<>(map.collectEntries { key, value ->
      return ["${key}": value.toMap()]
    })
    file.text = new JsonBuilder(toFileMap).toPrettyString()
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
    def value = map.get(key)
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
}
