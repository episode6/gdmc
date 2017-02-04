package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.task.GdmcResolveTask
import com.episode6.hackit.gdmc.util.GdmcLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency

/**
 *
 */
class GdmcTasksPlugin implements Plugin<Project> {

  Project project
  DependencyMap mapper

  @Override
  void apply(Project project) {
    this.project = project
    mapper = project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap

    project.extensions.create("gdmcLogger", GdmcLogger)

    project.task("gdmcResolve", type: GdmcResolveTask) {
      dependencies = {
        return findExternalDependencies {!it.version && !mapper.lookup(it.key)}
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    project.task("gdmcImport", type: GdmcResolveTask) {
      dependencies = {
        return findExternalDependencies {it.version} // TODO: should we only import missing deps, or overrite existing deps?
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    //import, importTransitive, upgrade, upgradeTransitive, upgradeAll
  }

  Collection<GdmcDependency> findExternalDependencies(Closure filter) {
    return project.configurations.collectMany({ Configuration config ->
      return config.dependencies.findAll {
        it instanceof ExternalDependency
      }.collect {
        GdmcDependency.from(it)
      }.findAll(filter)
    })
  }
}
