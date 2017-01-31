package com.episode6.hackit.gdmc.task

import groovy.json.JsonBuilder
import groovy.transform.Memoized
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile

/**
 *
 */
abstract class AbstractGdmcTask extends DefaultTask {

  @OutputFile @Memoized
  File getOutputFile() {
    return project.file("${project.buildDir}/${name}.json")
  }

  protected void writeJsonToOutputFile(Object obj) {
    outputFile.text = new JsonBuilder(obj).toString()
  }
}
