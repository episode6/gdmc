package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests the gdmcImport task
 */
class GdmcImportTest extends Specification {

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test import from nothing"(String plugin) {
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
   compile 'org.mockito:mockito-core:2.7.0'
   
   testCompile(group: 'org.spockframework', name: 'spock-core', version: '1.1-groovy-2.4-rc-2')  {
    exclude module: 'groovy-all'
  }
}
"""
    when:
    def result = test.runTask("gdmcImport")

    then:
    result.task(":gdmcImport").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        version == "2.7.0"
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        version == "0.1.7.2"
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        version == "1.1-groovy-2.4-rc-2"
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test import from nothing mutli-project"(String plugin) {
    given:
    setupMultiProject(test, plugin, [
        mockitoVersion: ':2.7.0',
        chopVersion: ':0.1.7.2',
        spockVersion: ':1.1-groovy-2.4-rc-2'])

    when:
    def result = test.runTask("gdmcImport")

    then:
    result.task(":javalib:gdmcImport").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcImport").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        version == "2.7.0"
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        version == "0.1.7.2"
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        version == "1.1-groovy-2.4-rc-2"
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
