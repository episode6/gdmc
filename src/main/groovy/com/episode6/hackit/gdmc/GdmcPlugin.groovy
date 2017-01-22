package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.throwable.GdmcPluginMissingException
import org.gradle.api.Nullable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

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
        println "missing dependencies: ${dependencies.missingDependencies}"

        Set<String> uniqueDependencies = project.configurations.collectMany {config ->
          return config.dependencies.findAll {
            it instanceof ExternalDependency
          }.collect {
            return "${it.group}:${it.name}:${it.version}"
          }
        }
        uniqueDependencies = project.configurations.collectMany { config ->
          config.incoming.resolutionResult.allDependencies.findAll {
            it instanceof ResolvedDependencyResult &&
                it.selected.id instanceof ModuleComponentIdentifier
          }.collect { ResolvedDependencyResult dep ->
            ModuleComponentIdentifier ident = dep.selected.id
            "${ident.group}:${ident.module}:${ident.version}"
          }
        }
//        project.configurations.all {
//          def deps = incoming.resolutionResult.allDependencies.collect {it.selected.id.displayName}
//          uniqueDependencies.addAll(deps)
//        }
        println("found unique deps: ${uniqueDependencies}")
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
