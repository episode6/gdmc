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
    dependencyMap = new JsonSlurper().parse(new File(gdmcDir, "gdmc.json"))
    println "depMap: ${dependencyMap}"

    Project.metaClass.gdmc = { key ->
      return lookupDependency(key)
    }
  }

  private String lookupDependency(String key) {
    def valueNode = dependencyMap.get(key)
    if (valueNode == null) {
      throw new RuntimeException("MISSING DEP: ${key} - PUT A REAL EXCEPTION HERE")
    }

    String alias = valueNode.get("alias")
    if (alias != null) {
      return lookupDependency(alias)
    }

    return "${valueNode.groupId}:${valueNode.artifactId}:${valueNode.version}"
  }
}
