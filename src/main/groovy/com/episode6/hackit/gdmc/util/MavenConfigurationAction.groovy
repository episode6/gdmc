package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.Action
import org.gradle.api.artifacts.maven.MavenPom

/**
 * Action to configure the mavenPom and ensure it has properly mapped versions
 */
class MavenConfigurationAction implements Action<MavenPom>, AbstractActionTrait {

  VersionMapperAction versionMapperAction

  MavenConfigurationAction(Map opts) {
    this.project = opts.project
    versionMapperAction = new VersionMapperAction(project: project)
  }

  @Override
  void execute(MavenPom mavenPom) {
    List<Map> aliasDeps = new LinkedList()
    mavenPom.dependencies.collect {
      new DependencyResolveDetailsWrapper(it)
    }.each {
      GdmcDependency unMapped = GdmcDependency.from(it.requested)
      if (dependencyMap.isAlias(unMapped.key)) {
        aliasDeps.add([rawDep: it.dependencyDelegate, gdmcDep: unMapped])
      } else {
        versionMapperAction.execute(it)
      }
    }

    aliasDeps.each {
      mavenPom.dependencies.remove(it.rawDep)
      List<GdmcDependency> mappedDeps = dependencyMap.lookup(it.gdmcDep.key)
      mappedDeps.each { GdmcDependency dep ->
        def mavenDep = it.rawDep.clone()
        mavenDep.groupId = dep.groupId
        mavenDep.artifactId = dep.artifactId
        mavenDep.version = dep.version
        mavenPom.dependencies.add(mavenDep)
      }
    }
  }
}
