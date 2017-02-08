package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.task.GdmcResolveTask
import com.episode6.hackit.gdmc.util.GdmcLogger
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Common plugin for both the main gdmc plugin and the gdmc-spring-compat.
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
        return findExternalDependencies {
          it.version && (overwrite || !mapper.lookup(it.key))
        }
      }
      doLast {
        mapper.applyFile(outputFile, { String key, GdmcDependency dep ->
          return overwrite || !mapper.lookup(key)
        })
      }
    }

    project.task("gdmcImportTransitive", type: GdmcResolveTask) {
      resolveTransitive = true

      dependencies = {
        // Because this is importing transitive deps, we need to resolve
        // existing dependencies that are mapped as well as versioned ones
        // that may be unmapped. I.e. we can't filter anything out because it
        // might have transitive deps we don't know about
        return findExternalDependencies({true}).collectMany {
          if (it.version) {
            return [it]
          }
          List<GdmcDependency> mappedDeps = mapper.lookup(it.key)
          if (!mappedDeps) {
            GChop.w("Skipping unmapped dependency: %s", it)
            return []
          }
          return mappedDeps
        }
      }
      doLast {
        mapper.applyFile(outputFile, { String key, GdmcDependency dep ->
          return overwrite || !mapper.lookup(key)
        })
      }
    }

    // upgrade, upgradeAll importSelf, validateSelf

    // We can't add extra dependencies from inside the VersionMapperAction, so instead,
    // we look for any dependencies that are mapped to aliases, and resolve and add them here
    project.afterEvaluate {
      project.configurations.all(new Action<Configuration>() {
        @Override
        void execute(Configuration files) {
          files.dependencies.findAll {
            it instanceof ExternalDependency
          }.collect {
            GdmcDependency.from(it)
          }.findAll {
            mapper.isAlias(it.key)
          }.collectMany {
            return mapper.lookup(it.key)
          }.each {
            GChop.d("Adding %s to config %s because it is mapped via an alias", it, files)
            project.dependencies.add(files.name, it.toString())
          }
        }
      })
    }
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

  boolean getOverwrite() {
    return project.hasProperty("overwrite") && project.overwrite
  }
}
