package com.episode6.hackit.gdmc

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {
  void apply(Project project) {
    File scriptDir = new File(project.rootDir, "gdmc")
    GroovyScriptEngine scriptEngine = new GroovyScriptEngine([scriptDir] as String[], this.class.classLoader)

    scriptDir.listFiles().each { File file ->
      if(!file.name.endsWith(".groovy")) {
        return
      }

      println "trying to load class ${file.absolutePath}"
      Class clazz = scriptEngine.loadScriptByName(file.name)

      def obj = clazz.newInstance()
      println "output from testMethod(): ${obj.testMethod()}"
    }
  }
}
