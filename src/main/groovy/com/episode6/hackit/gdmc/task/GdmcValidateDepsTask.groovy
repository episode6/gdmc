package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.exception.GdmcDependencyMismatchException
import com.episode6.hackit.gdmc.util.HasProjectTrait
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Since gdmc can't do anything about buildscript dependencies, the least we can do is validate them...
 *
 * This task checks your buildscript dependencies, and if any of them are mapped in your gdmc file,
 * we validate that the versions match. While this isn't as nice as automatically mapping the versions
 * for you, it should help ensure your buildscript dependencies stay up to date when sharing a gdmc file.
 */
class GdmcValidateDepsTask extends DefaultTask implements VerificationTask, HasProjectTrait {

  @Input Closure<Boolean> required = {true}

  @Input boolean ignoreFailures = false

  @Input Closure<Collection<Dependency>> dependencies

  @TaskAction
  def validate() {
    String taskName = name
    if (!required.call()) {
      GChop.d("Validation not required, skipping task: ${taskName}")
      return
    }

    try {
      performValidation()
    } catch (GdmcDependencyMismatchException e) {
      if (ignoreFailures) {
        GChop.e(e, "Buildscript Validation Failed")
      } else {
        throw e
      }
    }
  }

  private void performValidation() {
    Map<GdmcDependency, String> errors = new HashMap<>();

        dependencies.call().findAll {it instanceof ExternalDependency && it.version != "+" && !it.version.contains("SNAPSHOT")}
        .collect {GdmcDependency.from(it)}
        .each {
      List<GdmcDependency> mappedDeps = dependencyMap.lookupWithOverrides(it.mapKey)
      int mapCount = mappedDeps.size();
      if (mapCount == 0) {
        return
      }
      if (mapCount > 1) {
        errors.put(it, "mapped as alias to ${mappedDeps}")
      }

      if (!mappedDeps.get(0).matches(it)) {
        errors.put(it, "mapped to ${mappedDeps.get(0).getFullMavenKey()}")
      }
    }

    if (!errors.isEmpty()) {
      throw new GdmcDependencyMismatchException(errors);
    }
  }
}
