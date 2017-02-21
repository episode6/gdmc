package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests the importSelf and validateSelf tasks
 */
class GdmcSelfTest extends Specification {

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test importSelf single-project"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin)

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example.testproject:sample-proj")) {
        groupId == "com.example.testproject"
        artifactId == "sample-proj"
        version == "0.0.1-SNAPSHOT"
      }
      size() == 1
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importSelf multi-project"(String plugin) {
    given:
    setupMultiProject(test, plugin)

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":javalib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example:javalib")) {
        groupId == "com.example"
        artifactId == "javalib"
        version == "0.0.1-SNAPSHOT"
      }
      with(get("com.example:groovylib")) {
        groupId == "com.example"
        artifactId == "groovylib"
        version == "0.0.1-SNAPSHOT"
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
