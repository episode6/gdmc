package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.data.GdmcDependency
import org.gradle.api.Action
import org.gradle.api.artifacts.maven.MavenPom

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

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
    GChop.d("Configuring mavenPom: %s", mavenPom.dependencies)
    List<Map> aliasDeps = new LinkedList()
    mavenPom.dependencies.collect {
      new DependencyResolveDetailsWrapper(it)
    }.each {
      GdmcDependency unMapped = GdmcDependency.from(it.requested)
      GChop.d("Configuring maven dependency: %s", unMapped)
      if (dependencyMap.isAlias(unMapped.key)) {
        GChop.d("dependency appears to be an alias, skipping for now")
        aliasDeps.add([rawDep: it.dependencyDelegate, gdmcDep: unMapped])
      } else {
        GChop.d("dependency is not an alias, trying to map.")
        versionMapperAction.execute(it)
      }
    }

    aliasDeps.each {
      boolean removed = mavenPom.dependencies.remove(it.rawDep)

      // Everything below here might be totally unnecessary, it looks like
      // maven picks up new deps that we explicitly add (via alias mapping),
      // it just doesn't pick up changes made via the VersionMapperAction.
      GChop.d("Tried to remove alias dep, success: %s", removed)
      List<GdmcDependency> mappedDeps = dependencyMap.lookup(it.gdmcDep.key)
      mappedDeps.each { GdmcDependency dep ->
        if (mavenPom.dependencies.find { md ->
          md.groupId == dep.groupId &&
              md.artifactId == dep.artifactId &&
              md.version == dep.version &&
              md.scope == it.rawDep.scope
        } != null) {
          return
        }
        def mavenDep = it.rawDep.clone()
        mavenDep.groupId = dep.groupId
        mavenDep.artifactId = dep.artifactId
        mavenDep.version = dep.version
        mavenPom.dependencies.add(mavenDep)
        GChop.d("Added (aliased) maven dep for %s", dep)
      }
    }
  }
}
