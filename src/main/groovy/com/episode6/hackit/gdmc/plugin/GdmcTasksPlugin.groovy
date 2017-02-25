package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.task.GdmcResolveTask
import com.episode6.hackit.gdmc.task.GdmcValidateSelfTask
import com.episode6.hackit.gdmc.util.*
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.plugins.MavenPlugin

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
    project.convention.plugins.gdmcConvention = new GdmcConvention()

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
        return findMappedExternalDependencies()
      }
      doLast {
        mapper.applyFile(outputFile, { String key, GdmcDependency dep ->
          return overwrite || !mapper.lookup(key)
        })
      }
    }

    project.task("gdmcUpgrade", type: GdmcResolveTask) {
      dependencies = {
        return findMappedExternalDependencies().collect { GdmcDependency dep ->
          return dep.withoutVersion()
        }
      }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    project.task("gdmcUpgradeAll", type: GdmcResolveTask) {
      dependencies = { mapper.validDependencies.collect {it.withoutVersion()} }
      doLast {
        mapper.applyFile(outputFile)
      }
    }

    project.task("gdmcImportSelf") {
      doFirst {
        TaskAssertions.assertLonelyTask(delegate)
      }
      doLast {
        mapper.put(GdmcDependency.from(project))
      }
    }

    project.task("gdmcValidateSelf", type: GdmcValidateSelfTask)

    project.afterEvaluate {

      project.tasks.findByPath("check")?.dependsOn project.gdmcValidateSelf
      project.tasks.findByPath("test")?.dependsOn project.gdmcValidateSelf

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
            !it.version && mapper.isAlias(it.key)
          }.collectMany {
            return mapper.lookup(it.key)
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

  Collection<GdmcDependency> findMappedExternalDependencies() {
    return findExternalDependencies({true}).collectMany {
      return it.version ? [it] : mapper.lookup(it.key)
    }
  }

  boolean getOverwrite() {
    return ProjectProperties.overwrite(project)
  }
}
