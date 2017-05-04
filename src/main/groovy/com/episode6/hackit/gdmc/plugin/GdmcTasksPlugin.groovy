package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.task.GdmcResolveTask
import com.episode6.hackit.gdmc.task.GdmcValidateBuildscriptDepsTask
import com.episode6.hackit.gdmc.task.GdmcValidateSelfTask
import com.episode6.hackit.gdmc.util.*
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.plugins.MavenPlugin

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Common plugin for both the main gdmc plugin and the gdmc-spring-compat.
 */
class GdmcTasksPlugin implements Plugin<Project>, HasProjectTrait {

  static final GDMC_RESOLVE_TASK_GROUP = "gdmc resolve"
  static final VERIFICATION_TASK_GROUP = "verification"

  Project project

  @Override
  void apply(Project project) {
    this.project = project

    project.extensions.create("gdmcLogger", GdmcLogger)
    project.convention.plugins.gdmcConvention = new GdmcConvention()

    project.task("gdmcResolve", type: GdmcResolveTask) {
      description = "Resolves any missing dependencies in project '${project.name}' and adds them to gdmc."
      group = GDMC_RESOLVE_TASK_GROUP
      dependencies = {
        return findExternalDependencies {!it.version && !dependencyMap.lookupFromSource(it.key)}
      }
      doLast {
        dependencyMap.applyFile(outputFile)
      }
    }

    project.task("gdmcImport", type: GdmcResolveTask) {
      description = "Imports fully-qualified dependencies from this project into gdmc."
      group = GDMC_RESOLVE_TASK_GROUP
      dependencies = {
        return findExternalDependencies {
          it.version && (overwrite || !dependencyMap.lookupFromSource(it.key))
        }
      }
      doLast {
        dependencyMap.applyFile(outputFile, { String key, GdmcDependency dep ->
          return overwrite || !dependencyMap.lookupFromSource(key)
        })
      }
    }

    project.task("gdmcImportTransitive", type: GdmcResolveTask) {
      description = "Imports fully-qualified dependencies and all transitive dependencies into gdmc."
      group = GDMC_RESOLVE_TASK_GROUP
      resolveTransitive = true

      dependencies = {
        // Because this is importing transitive deps, we need to resolve
        // existing dependencies that are mapped as well as versioned ones
        // that may be unmapped. I.e. we can't filter anything out because it
        // might have transitive deps we don't know about
        return findSourceMappedExternalDependencies()
      }
      doLast {
        dependencyMap.applyFile(outputFile, { String key, GdmcDependency dep ->
          return overwrite || !dependencyMap.lookupFromSource(key)
        })
      }
    }

    project.task("gdmcUpgrade", type: GdmcResolveTask) {
      description = "Resolves the latest versions of current project dependencies and apply those new versions to gdmc."
      group = GDMC_RESOLVE_TASK_GROUP
      dependencies = {
        return findSourceMappedExternalDependencies().collect { GdmcDependency dep ->
          return dep.withoutVersion()
        }
      }
      doLast {
        dependencyMap.applyFile(outputFile)
      }
    }

    project.task("gdmcUpgradeAll", type: GdmcResolveTask) {
      description = "Resolves the latest versions of all entries in gdmc and apply those new versions to gdmc."
      group = GDMC_RESOLVE_TASK_GROUP
      dependencies = { dependencyMap.validDependencies.collect {it.withoutVersion()} }
      doLast {
        dependencyMap.applyFile(outputFile)
      }
    }

    project.task("gdmcImportSelf") {
      description = "Imports '${project.group}:${project.name}:${project.version}' as a dependency into gdmc."
      group = GDMC_RESOLVE_TASK_GROUP
      doFirst {
        TaskAssertions.assertLonelyTask(delegate)
      }
      doLast {
        dependencyMap.put(GdmcDependency.from(project))
      }
    }

    project.task("gdmcValidateSelf", type: GdmcValidateSelfTask) {
      description = "Verifies that the gdmc file contains a valid mapping for '${project.group}:${project.name}:${project.version}'."
      group = VERIFICATION_TASK_GROUP
    }

    project.task("gdmcValidateBuildscriptDeps", type: GdmcValidateBuildscriptDepsTask) {
      description = "Verifies that any buildscript dependencies that are mapped in gdmc have the correct mapped versions."
      group = VERIFICATION_TASK_GROUP
    }

    project.afterEvaluate {

      project.tasks.findByPath("check")?.dependsOn project.gdmcValidateSelf, project.gdmcValidateBuildscriptDeps
      project.tasks.findByPath("test")?.dependsOn project.gdmcValidateSelf, project.gdmcValidateBuildscriptDeps

      // We can't add extra dependencies from inside the VersionMapperAction, so instead,
      // we look for any dependencies that are mapped to aliases, and resolve and add them here
      project.configurations.all(new Action<Configuration>() {
        @Override
        void execute(Configuration files) {
          files.dependencies.findAll {
            it instanceof ExternalDependency
          }.collect {
            GdmcDependency.from(it)
          }.findAll {
            !it.version && dependencyMap.isOverrideAlias(it.key)
          }.collectMany {
            return dependencyMap.lookupWithOverrides(it.key)
          }.each {
            GChop.d("Adding %s to config %s because it is mapped via an alias", it, files)
            project.dependencies.add(files.name, it.toString())
          }
        }
      })

      if (project.plugins.findPlugin(MavenPlugin)) {
        MavenConfigurationAction mavenConfigurationAction = new MavenConfigurationAction(project: project)
        project.tasks.findByPath("uploadArchives")?.repositories {
          mavenDeployer.pom.whenConfigured(mavenConfigurationAction)
        }
        project.tasks.findByPath("install")?.repositories {
          mavenInstaller.pom.whenConfigured(mavenConfigurationAction)

        }
      }
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

  Collection<GdmcDependency> findSourceMappedExternalDependencies() {
    return findExternalDependencies({true}).collectMany {
      return it.version ? [it] : dependencyMap.lookupFromSource(it.key)
    }
  }

  boolean getOverwrite() {
    return ProjectProperties.overwrite(project)
  }
}
