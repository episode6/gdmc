package com.episode6.hackit.gdmc

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {

  def dependencyMap

  void apply(Project project) {
    File gdmcDir = new File(project.rootDir, "gdmc")
    try {
      dependencyMap = new JsonSlurper().parse(new File(gdmcDir, "gdmc.json"))
    } catch (Throwable t) {
      t.printStackTrace()
      throw new RuntimeException("Error parsing jsonFile!", t)
    }
    println "depMap: ${dependencyMap}"

    Project.metaClass.gdmc = { key ->
      if (key instanceof Map) {
        return lookupDependencyFromMap(key)
      }
      return lookupDependencyFromKey(key)
    }
  }

  private Object lookupDependencyFromMap(Map map) {
    println "looking up from Map: ${map}"
    String key = "${map.get('group')}:${map.get('name')}"
    String internalVersion = map.get("version")
    if (internalVersion != null) {
      key = "${key}:${internalVersion}"
    }
    return lookupDependencyFromKey(key)
  }

  private Object lookupDependencyFromKey(String key) {
    println "looking up key ${key}"
    def valueNode = dependencyMap.get(key)
    if (valueNode == null) {
      throw new RuntimeException("MISSING DEP: ${key} - PUT A REAL EXCEPTION HERE")
    }

    def alias = valueNode.get("alias")
    if (alias == null) {
      return "${valueNode.groupId}:${valueNode.artifactId}:${valueNode.version}"
    }
    println("Alias: ${alias.class.toString()} = ${alias}")
    if (alias instanceof List) {
      List<String> resolvedKeys = new ArrayList<>()
      alias.each { String it ->
        println "found alias key: ${it}"
        def resolved = lookupDependencyFromKey(it)
        println("resolved to ${resolved.class.toString()} = ${resolved}")
        if (resolved instanceof String[]) {
          resolvedKeys.addAll(resolved)
        } else {
          resolvedKeys.add(resolved)
        }
      }
      return (String[])resolvedKeys.toArray()
    }
    return lookupDependencyFromKey(alias)
  }
}
