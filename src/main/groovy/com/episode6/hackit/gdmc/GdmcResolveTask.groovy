package com.episode6.hackit.gdmc

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class GdmcResolveTask extends DefaultTask {

  @TaskAction
  def resolveMissingDependencies() {
    println "taskList: ${project.gradle.taskGraph.allTasks}"
    println "missing dependencies: ${dependencies.missingDependencies}"

    Set<String> uniqueDependencies = project.configurations.collectMany {config ->
      return config.dependencies.findAll {
        it instanceof ExternalDependency
      }.collect {
        return "${it.group}:${it.name}:${it.version}"
      }
    }
//    uniqueDependencies = project.configurations.collectMany { config ->
//      config.incoming.resolutionResult.allDependencies.findAll {
//        it instanceof ResolvedDependencyResult &&
//            it.selected.id instanceof ModuleComponentIdentifier
//      }.collect { ResolvedDependencyResult dep ->
//        ModuleComponentIdentifier ident = dep.selected.id
//        "${ident.group}:${ident.module}:${ident.version}"
//      }
//    }
    println("found unique deps: ${uniqueDependencies}")

  }

  GdmcDependencyContainer getDependencies() {
    return project.plugins.getPlugin(GdmcPlugin).dependencies
  }
}
