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
class Tasks {
  static void init(Project project) {
    DependencyMap mapper = project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap

    project.task("gdmcResolveMissing", type: GdmcResolveTask) {
      keys = {
        return findMissingDependencyKeys(project, mapper)
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }
  }

  static Collection<String> findMissingDependencyKeys(Project project, DependencyMap mapper) {
    return project.configurations.collectMany { Configuration config ->
      return config.dependencies.collectMany { Dependency dep ->
        GdmcDependency unMapped = GdmcDependency.from(dep)
        if (unMapped.version || mapper.lookup(unMapped.key)) {
          return []
        }
        return [unMapped.key]
      }
    }
  }
}
