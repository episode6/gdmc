package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.throwable.GdmcParseException
import groovy.json.JsonSlurper

/**
 * Container for the gdmc dependency hashmap
 */
class GdmcDependencyContainer {
  Map map = new LinkedHashMap()
  List<String> missingDependencies = new LinkedList<>()

  void applyFile(File jsonFile) {
    try {
      def jsonMap = new JsonSlurper().parse(jsonFile)
      map.putAll(jsonMap)
    } catch (Throwable t) {
      throw new GdmcParseException("Failed to apply gdmc file: ${jsonFile.absolutePath}", t)
    }
  }

  Object lookup(Object key) {
    if (key instanceof Map) {
      return lookupMap(key)
    }
    return lookupKey((String)key)
  }

  private Object lookupMap(Map params) {
    String key = "${params.get('group')}:${params.get('name')}"
    String internalVersion = params.get("version")
    if (internalVersion != null) {
      key = "${key}:${internalVersion}"
    }
    return lookupKey(key)
  }

  private Object lookupKey(String key) {
    def valueNode = map.get(key)
    if (valueNode == null) {
      missingDependencies.add(key)
      return "${key}:+"
//      throw new RuntimeException("MISSING DEP: ${key} - PUT A REAL EXCEPTION HERE")
    }

    def alias = valueNode.get("alias")
    if (alias == null) {
      return "${valueNode.groupId}:${valueNode.artifactId}:${valueNode.version}"
    }

    if (alias instanceof List) {
      List<String> resolvedKeys = new ArrayList<>()
      alias.each { String it ->
        def resolved = lookupKey(it)
        if (resolved instanceof String[]) {
          resolvedKeys.addAll(resolved)
        } else {
          resolvedKeys.add((String)resolved)
        }
      }
      return (String[])resolvedKeys.toArray()
    }
    return lookupKey((String)alias)
  }
}
