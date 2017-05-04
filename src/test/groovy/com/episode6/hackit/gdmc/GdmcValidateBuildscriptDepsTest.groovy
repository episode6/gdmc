package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests the gdmcValidateBuildscriptDeps task
 */
class GdmcValidateBuildscriptDepsTest extends Specification {

  static final String buildFile(String plugins, Map opts = [:]) {
    """
buildscript {
  repositories {
    maven {url "https://oss.sonatype.org/content/repositories/snapshots/"}
    jcenter()
  }
  dependencies {
    classpath 'com.episode6.hackit.deployable:deployable:${opts.deployableVersion ?: '0.1.2'}'
  }
}

plugins {
  id 'groovy'
${plugins}
}

gdmcLogger {
 enable()
}

group = 'com.example.testproject'
version = '${opts.version ?: '0.0.1-SNAPSHOT'}'

repositories {
  jcenter()
}

"""
  }

  static final String gdmcContents(String deployableVersion) {
    return """
{
    "com.episode6.hackit.deployable:deployable": {
        "groupId": "com.episode6.hackit.deployable",
        "artifactId": "deployable",
        "version": "${deployableVersion}"
    }
}
"""
  }

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test validate passes when unmapped"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFile(plugin)

    when:
    def result = test.build("gdmcValidateBuildscriptDeps")

    then:
    result.task(":gdmcValidateBuildscriptDeps").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validate passes when mapped correctly"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFile(plugin, [deployableVersion: "0.1.2"])
    test.gdmcJsonFile << gdmcContents("0.1.2")

    when:
    def result = test.build("gdmcValidateBuildscriptDeps")

    then:
    result.task(":gdmcValidateBuildscriptDeps").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validate fails when mapped incorrectly"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFile(plugin, [deployableVersion: "0.1.2"])
    test.gdmcJsonFile << gdmcContents("0.1.5")

    when:
    def result = test.buildAndFail("gdmcValidateBuildscriptDeps")

    then:
    result.task(":gdmcValidateBuildscriptDeps").outcome == TaskOutcome.FAILED
    result.output.contains("Buildscript Dependency Version Mismatch")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validate passes when mapped correctly thanks to overrides"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFile(plugin, [deployableVersion: "0.1.2"])
    test.gdmcJsonFile << gdmcContents("0.1.5")
    test.singleGdmcOverrideFile() << gdmcContents("0.1.2")

    when:
    def result = test.build("gdmcValidateBuildscriptDeps")

    then:
    result.task(":gdmcValidateBuildscriptDeps").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validate passes using snapshot"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFile(plugin, [deployableVersion: "0.1.4-SNAPSHOT"])
    test.gdmcJsonFile << gdmcContents("0.1.5")

    when:
    def result = test.build("gdmcValidateBuildscriptDeps")

    then:
    result.task(":gdmcValidateBuildscriptDeps").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validate passes using plus"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFile(plugin, [deployableVersion: "+"])
    test.gdmcJsonFile << gdmcContents("0.1.4")

    when:
    def result = test.build("gdmcValidateBuildscriptDeps")

    then:
    result.task(":gdmcValidateBuildscriptDeps").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
