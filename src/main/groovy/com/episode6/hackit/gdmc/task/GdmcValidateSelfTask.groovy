package com.episode6.hackit.gdmc.task

import com.episode6.hackit.gdmc.data.DependencyMap
import com.episode6.hackit.gdmc.data.GdmcDependency
import com.episode6.hackit.gdmc.exception.GdmcSelfValidationException
import com.episode6.hackit.gdmc.plugin.GdmcRootPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import static com.episode6.hackit.gdmc.util.GdmcLogger.GChop

/**
 * Task that ensures your project's gdmc file always references it's current version
 */
class GdmcValidateSelfTask extends DefaultTask {

  @Input Closure<Boolean> required = {!project.version.contains("SNAPSHOT")}

  @TaskAction
  def validate() {
    if (!required.call()) {
      GChop.d("Validation not required, skipping task: ${name}")
      return
    }

    DependencyMap mapper = project.rootProject.plugins.getPlugin(GdmcRootPlugin).dependencyMap
    GdmcDependency selfDep = GdmcDependency.from(project)

    mapper.lookup(selfDep.key).with {
      if (size() == 1 && get(0) == selfDep) {
        GChop.d("Succesfully validated ${selfDep} in gdmc.")
      } else {
        throw new GdmcSelfValidationException(selfDep, delegate)
      }
    }
  }

}
