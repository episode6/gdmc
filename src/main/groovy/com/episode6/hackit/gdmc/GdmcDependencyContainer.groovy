package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.throwable.GdmcParseException
import groovy.json.JsonSlurper

/**
 * Container for the gdmc dependency hashmap
 */
class GdmcDependencyContainer {
  Map map = new LinkedHashMap()

  void applyFile(File jsonFile) {
    try {
      def jsonMap = new JsonSlurper().parse(jsonFile)
      map.putAll(jsonMap)
    } catch (Throwable t) {
      throw new GdmcParseException("Failed to apply gdmc file: ${jsonFile.absolutePath}", t)
    }
  }

  Object lookup(Map map) {
    println "looking up from Map: ${map}"
    String key = "${map.get('group')}:${map.get('name')}"
    String internalVersion = map.get("version")
    if (internalVersion != null) {
      key = "${key}:${internalVersion}"
    }
    return lookup(key)
  }

  Object lookup(String key) {
    println "looking up key ${key}"
    def valueNode = map.get(key)
    if (valueNode == null) {
      throw new RuntimeException("MISSING DEP: ${key} - PUT A REAL EXCEPTION HERE")
    }
    println "has value: ${valueNode}"

    def alias = valueNode.get("alias")
    if (alias == null) {
      return "${valueNode.groupId}:${valueNode.artifactId}:${valueNode.version}"
    }
    println("Alias: ${alias.class.toString()} = ${alias}")
    if (alias instanceof List) {
      List<String> resolvedKeys = new ArrayList<>()
      alias.each { String it ->
        println "found alias key: ${it}"
        def resolved = lookup(it)
        println("resolved to ${resolved.class.toString()} = ${resolved}")
        if (resolved instanceof String[]) {
          resolvedKeys.addAll(resolved)
        } else {
          resolvedKeys.add((String)resolved)
        }
      }
      return (String[])resolvedKeys.toArray()
    }
    return lookup((String)alias)
  }
}
