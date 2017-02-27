package com.episode6.hackit.gdmc.plugin

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.util.ProjectProperties
import com.episode6.hackit.gdmc.util.VersionMapperAction
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Plugin to make gdmc compatible with Spring's dependency management plugin
 * https://github.com/spring-gradle-plugins/dependency-management-plugin
 */
class GdmcSpringsCompatPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    DependencyMap dependencyMap = GdmcRootPlugin.ensureInit(project).dependencyMap
    project.plugins.apply(GdmcTasksPlugin)

    // If we see the overwrite property on the project, we assume that we are performing an import task
    // and skip the step of adding our gdmc dependencies to spring's dependency manager. We do this so
    // we may bypass spring's dependency-mapping actions and see the true transitive dependency versions
    // we are looking for.
    if (!ProjectProperties.overwrite(project)) {
      project.dependencyManagement {
        dependencies {
          dependencyMap.validDependencies.each {
            GChop.d("added dependency %s to dependencyManager", it)
            dependency it.toString()
          }
        }
      }
    }

    // While we leave most of the dependency mapping up to the springs plugin, we must ensure any aliases
    // get mapped properly
    VersionMapperAction aliasMapper = new VersionMapperAction(project: project) {
      @Override
      boolean shouldSkipMappingVersion(GdmcDependency unMapped) {
        // if forceResolve param is set, we should apply this to everything w/o a version
        return forceResolve() ? super.shouldSkipMappingVersion(unMapped) : !dependencyMap.isAlias(unMapped.key)
      }
    }
    project.configurations.all(new Action<Configuration>() {
      @Override
      void execute(Configuration config) {
        config.resolutionStrategy.eachDependency(aliasMapper)
      }
    })
  }
}
