package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.DependencyMap
import com.episode6.hackit.gdmc.GdmcRootPlugin
import com.episode6.hackit.gdmc.json.GdmcDependency
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency

/**
 *
 */
class GdmcTasksPlugin {
  static void init(Project project) {
    DependencyMap mapper = project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap

    project.task("gdmcResolveMissing", type: GdmcResolveTask) {
      dependencies = {
        return findMissingDependencies(project, mapper)
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    project.task("gdmcImport", type: GdmcResolveTask) {
      dependencies = {
        return findVersionedDependencies(project)
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    //import, importTransitive, upgrade, upgradeTransitive, upgradeAll
  }

  static Collection<GdmcDependency> findMissingDependencies(Project project, DependencyMap mapper) {
    return project.configurations.collectMany { Configuration config ->
      return config.dependencies.collect {GdmcDependency.from(it)}.findAll {
        !it.version && !mapper.lookup(it.key)
      }
    }
  }

  static Collection<GdmcDependency> findVersionedDependencies(Project project) {
    return project.configurations.collectMany { Configuration config ->
      return config.dependencies.collect {GdmcDependency.from(it)}.findAll {
        it.version
      }
    }
  }
}
