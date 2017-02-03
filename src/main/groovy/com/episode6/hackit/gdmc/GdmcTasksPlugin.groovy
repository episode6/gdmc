package com.episode6.hackit.gdmc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

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
        return projectDependencies {!it.version && !mapper.lookup(it.key)}
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    project.task("gdmcImport", type: GdmcResolveTask) {
      dependencies = {
        return projectDependencies {it.version} // TODO: should we only import missing deps, or overrite existing deps?
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    //import, importTransitive, upgrade, upgradeTransitive, upgradeAll
  }

  Collection<GdmcDependency> projectDependencies(Closure filter) {
    return project.configurations.collectMany { Configuration config ->
      return config.dependencies.collect {GdmcDependency.from(it)}.findAll(filter)
    }
  }
}
