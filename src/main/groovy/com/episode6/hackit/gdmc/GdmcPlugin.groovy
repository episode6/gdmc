package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.throwable.GdmcPluginMissingException
import org.gradle.api.Nullable
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Dependency Management Center Plugin
 */
class GdmcPlugin implements Plugin<Project> {

  static final DEFAULT_FOLDER_NAME = "gdmc"
  static final DEFAULT_FILE_NAME = "gdmc.json"

  GdmcDependencyContainer dependencies

  void apply(Project project) {
    dependencies = new GdmcDependencyContainer()
    File gdmcFile = findGdmcFile(project.rootDir)
    if (gdmcFile != null) {
      println "applying file ${gdmcFile.absolutePath}"
      dependencies.applyFile(gdmcFile)
    }

    project.task("gdmcResolve") {
      doLast {
        println "taskList: ${project.gradle.taskGraph.allTasks}"
      }
    }

    Project.metaClass.gdmc = { key ->
      GdmcPlugin gdmcPlugin = plugins.findPlugin(GdmcPlugin)
      if (gdmcPlugin == null) {
        throw new GdmcPluginMissingException()
      }
      return gdmcPlugin.dependencies.lookup(key)
    }
  }

  private @Nullable File findGdmcFile(File rootDir) {
    File gdmcFile = new File(rootDir, DEFAULT_FILE_NAME)
    if (gdmcFile.exists()) {
      println "root gdmc file exists, returning ${gdmcFile.absolutePath}"
      return gdmcFile
    }

    File gdmcFolder = new File(rootDir, DEFAULT_FOLDER_NAME)
    if (!gdmcFolder.exists() || !gdmcFolder.isDirectory()) {
      println "gdmc folder does not exist, returing null ${gdmcFolder.absolutePath}"
      return null
    }

    gdmcFile = new File(gdmcFolder, DEFAULT_FILE_NAME)
    println "final gdmc file exists: ${gdmcFile.exists()}, path: ${gdmcFile.absolutePath}"
    return gdmcFile.exists() ? gdmcFile : null;
  }

}
