package com.episode6.hackit.gdmc.util

import com.episode6.hackit.gdmc.exception.GdmcIllegalTaskGroupingException
import org.gradle.api.Task

/**
 * Assertions to be used in tasks
 */
class TaskAssertions {

  private static final List<String> LEGAL_TASKS = ["clean"]

  /**
   * Assert that the provided task is alone in the task tree
   */
  static void assertLonelyTask(Task task) {
    List<String> legalSuffixes = LEGAL_TASKS + task.name

    def illegalTask = task.project.gradle.taskGraph.allTasks.find { runningTask ->
      legalSuffixes.find { legalSuffix ->
        runningTask.name.endsWith(legalSuffix)
      } == null
    }

    if (illegalTask) {
      throw new GdmcIllegalTaskGroupingException(task, illegalTask)
    }
  }
}
