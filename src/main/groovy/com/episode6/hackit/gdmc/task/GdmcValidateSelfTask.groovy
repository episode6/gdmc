package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.exception.GdmcSelfValidationException
import com.episode6.hackit.gdmc.util.HasProjectTrait
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Task that ensures your project's gdmc file always references it's current version
 */
class GdmcValidateSelfTask extends DefaultTask implements VerificationTask, HasProjectTrait {

  @Input Closure<Boolean> required = {!project.version.contains("SNAPSHOT")}

  @Input boolean ignoreFailures = false

  @TaskAction
  def validate() {
    String taskName = name
    if (!required.call()) {
      GChop.d("Validation not required, skipping task: ${taskName}")
      return
    }

    GdmcDependency selfDep = GdmcDependency.from(project)
    dependencyMap.lookupWithOverrides(selfDep.key).with {
      if (size() == 1 && get(0) == selfDep) {
        GChop.d("Succesfully validated ${selfDep} in gdmc.")
      } else {
        GdmcSelfValidationException failure = new GdmcSelfValidationException(selfDep, delegate)
        if (ignoreFailures) {
          GChop.e(failure, "Ignoring failure in task %s", taskName)
        } else {
          throw failure
        }
      }
    }
  }
}
