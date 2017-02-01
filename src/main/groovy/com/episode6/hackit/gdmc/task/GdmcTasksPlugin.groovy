package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.DependencyMap
import com.episode6.hackit.gdmc.GdmcRootPlugin
import com.episode6.hackit.gdmc.json.GdmcDependency
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

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

    project.task("gdmcResolveMissing", type: GdmcResolveTask) {
      dependencies = {
        return findMissingDependencies()
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    project.task("gdmcImport", type: GdmcResolveTask) {
      dependencies = {
        return findVersionedDependencies()
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    //import, importTransitive, upgrade, upgradeTransitive, upgradeAll
  }

  Collection<GdmcDependency> findMissingDependencies() {
    return project.configurations.collectMany { Configuration config ->
      return config.dependencies.collect {GdmcDependency.from(it)}.findAll {
        !it.version && !mapper.lookup(it.key)
      }
    }
  }

  Collection<GdmcDependency> findVersionedDependencies() {
    return project.configurations.collectMany { Configuration config ->
      return config.dependencies.collect {GdmcDependency.from(it)}.findAll {
        it.version
      }
    }
  }
}
