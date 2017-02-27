package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests the gdmcResolve task.
 */
class GdmcResolveTest extends Specification {

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test resolve missing dependencies"(String plugin) {
    given:
    test.gdmcJsonFile << "{}"
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
   testCompile(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""

    when:
    def result = test.build("gdmcResolve")

    then:
    result.task(":gdmcResolve").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        !version.contains("-SNAPSHOT")
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
      }
      size() == 3
      verifyJsonSortOrder((Map)delegate)
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "mutli-project test"(String plugin) {
    given:
    setupMultiProject(test, plugin)

    when:
    def result = test.build("gdmcResolve")

    then:
    result.task(":javalib:gdmcResolve").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcResolve").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()

    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        !version.contains("-SNAPSHOT")
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
      }
      size() == 3
      verifyJsonSortOrder((Map)delegate)
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test resolve tasks only runs by themselves"(String plugin, String taskName) {
    given:
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.createJavaFile(packageName: "com.episode6.testproject")

    when:
    def result = test.buildAndFail(taskName, "build")

    then:
    result.task(":${taskName}").outcome == TaskOutcome.FAILED
    result.task(":build") == null
    result.output.contains("Illegal task found")

    where:
    plugin                      | taskName
    GDMC_PLUGIN                 | TASK_RESOLVE
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_RESOLVE
    GDMC_PLUGIN                 | TASK_IMPORT
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT
    GDMC_PLUGIN                 | TASK_IMPORT_TRANS
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT_TRANS
    GDMC_PLUGIN                 | TASK_UPGRADE
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_UPGRADE
    GDMC_PLUGIN                 | TASK_UPGRADE_ALL
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_UPGRADE_ALL
    GDMC_PLUGIN                 | TASK_IMPORT_SELF
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT_SELF
  }

  def "test resolve tasks only runs by themselves (multi-project)"(String plugin, String taskName) {
    given:
    setupMultiProject(test, plugin, [
        mockitoVersion: ':2.7.0',
        chopVersion: ':0.1.7.2',
        spockVersion: ':1.1-groovy-2.4-rc-2'])

    when:
    def result = test.buildAndFail(taskName, "build")

    then:
    result.task(":javalib:${taskName}")?.outcome == TaskOutcome.FAILED ||
        result.task(":groovylib:${taskName}")?.outcome == TaskOutcome.FAILED
    result.task(":javalib:build") == null
    result.task(":groovylib:build") == null
    result.output.contains("Illegal task found")

    where:
    plugin                      | taskName
    GDMC_PLUGIN                 | TASK_RESOLVE
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_RESOLVE
    GDMC_PLUGIN                 | TASK_IMPORT
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT
    GDMC_PLUGIN                 | TASK_IMPORT_TRANS
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT_TRANS
    GDMC_PLUGIN                 | TASK_UPGRADE
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_UPGRADE
    GDMC_PLUGIN                 | TASK_UPGRADE_ALL
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_UPGRADE_ALL
    GDMC_PLUGIN                 | TASK_IMPORT_SELF
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT_SELF
  }

  def "test resolve tasks see clean as a legal task (multi-project)"(String plugin, String taskName) {
    given:
    setupMultiProject(test, plugin, [
        mockitoVersion: ':2.7.0',
        chopVersion: ':0.1.7.2',
        spockVersion: ':1.1-groovy-2.4-rc-2'])

    when:
    def result = test.build("clean", taskName)

    then:
    result.task(":javalib:clean").outcome == TaskOutcome.UP_TO_DATE
    result.task(":groovylib:clean").outcome == TaskOutcome.UP_TO_DATE
    result.task(":javalib:${taskName}").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:${taskName}").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | taskName
    GDMC_PLUGIN                 | TASK_RESOLVE
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_RESOLVE
    GDMC_PLUGIN                 | TASK_IMPORT
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT
    GDMC_PLUGIN                 | TASK_IMPORT_TRANS
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT_TRANS
    GDMC_PLUGIN                 | TASK_UPGRADE
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_UPGRADE
    GDMC_PLUGIN                 | TASK_UPGRADE_ALL
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_UPGRADE_ALL
    GDMC_PLUGIN                 | TASK_IMPORT_SELF
    GDMC_SPRINGS_COMPAT_PLUGIN  | TASK_IMPORT_SELF
  }

  private static boolean verifyJsonSortOrder(Map json) {
    String lastKey = null
    json.keySet().each { key ->
      assert key > lastKey
      lastKey = key
    }
    return true
  }
}
