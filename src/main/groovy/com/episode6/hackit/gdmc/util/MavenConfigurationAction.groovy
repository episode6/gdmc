package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.Action
import org.gradle.api.artifacts.maven.MavenPom

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Action to configure the mavenPom and ensure it has properly mapped versions.
 * The maven plugin appear to respect new dependencies that are explicitly added
 * via alias-mapping, but it does not respect replacements made by the
 * versionMapperAction
 */
class MavenConfigurationAction implements Action<MavenPom>, AbstractActionTrait {

  VersionMapperAction versionMapperAction

  MavenConfigurationAction(Map opts) {
    this.project = opts.project
    versionMapperAction = new VersionMapperAction(project: project)
  }

  @Override
  void execute(MavenPom mavenPom) {
    GChop.d("Configuring mavenPom: %s", mavenPom.dependencies)
    List aliasDeps = new LinkedList()
    mavenPom.dependencies.findAll {
      // we only care if the version is missing
      !it.version
    }.collect {
      new DependencyResolveDetailsWrapper(it)
    }.each {
      GChop.d("Configuring maven dependency: %s", it)
      if (dependencyMap.isAlias(it.asGdmcDependency().key)) {
        GChop.d("dependency appears to be an alias, will be removed")
        aliasDeps.add(it.dependencyDelegate)
      } else {
        GChop.d("dependency is not an alias, trying to map.")
        versionMapperAction.execute(it)
      }
    }

    mavenPom.dependencies.removeAll(aliasDeps)
  }
}
