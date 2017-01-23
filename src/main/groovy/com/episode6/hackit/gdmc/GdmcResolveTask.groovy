package com.episode6.hackit.gdmc

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ComponentMetadata
import org.gradle.api.artifacts.ComponentSelection
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.specs.Specs
import org.gradle.api.tasks.TaskAction

/**
 *
 */
class GdmcResolveTask extends DefaultTask {


  GdmcDependencyContainer getDependencies() {
    return project.plugins.getPlugin(GdmcPlugin).dependencies
  }

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
    createConfig()
    println("found unique deps: ${uniqueDependencies}")

  }

  def createConfig() {
    Configuration config = project.configurations.create("gdmcConfig")
    config.resolutionStrategy { resolutionStrategy ->
      resolutionStrategy.componentSelection { rules ->
        rules.all { ComponentSelection selection, ComponentMetadata metadata ->
          println "component selection for ${selection.candidate.group}:${selection.candidate.module}:${selection.candidate.version} - status: ${metadata.status}"
          if (metadata.status == 'integration') {
            selection.reject("Component status ${metadata.status} rejected")
          }
        }
      }
    }
    dependencies.missingDependencies.each {
      project.dependencies.add("gdmcConfig", "${it}:+")
    }
    config.resolvedConfiguration.getFirstLevelModuleDependencies(Specs.SATISFIES_ALL).each {
      println "RESOLVED MODULE: ${it.module.id}"
    }
  }

}
