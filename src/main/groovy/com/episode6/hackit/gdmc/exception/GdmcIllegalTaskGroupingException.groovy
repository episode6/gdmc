package com.episode6.hackit.gdmc.exception

import org.gradle.api.GradleException
import org.gradle.api.Task

/**
 * Exception thrown when one of gdmc's resolve tasks are executed along side some other unrelated task.
 */
class GdmcIllegalTaskGroupingException extends GradleException {
  GdmcIllegalTaskGroupingException(Task resolveTask, Task illegalTask) {
    super("Task ${resolveTask.name} makes changes to build dependencies and is not allowed to be executed " +
        "along-side other build tasks. Illegal task found: ${illegalTask.name}.")
  }
}
