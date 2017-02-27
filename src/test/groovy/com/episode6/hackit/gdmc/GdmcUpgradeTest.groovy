package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests the gdmcUpgrade task
 */
class GdmcUpgradeTest extends Specification {

  private static final String PRE_SET_DEPENDENCIES = """
{
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.1"
   },
   "org.mockito:mockito-core": {
     "groupId": "org.mockito",
     "artifactId": "mockito-core",
     "version": "2.6.0"
   },
   "org.spockframework:spock-core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.1-groovy-2.4-rc-2"
   }
}
"""

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test single-project upgrade"(String plugin) {
    given:
    test.gdmcJsonFile << PRE_SET_DEPENDENCIES
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
  compile 'com.episode6.hackit.chop:chop-core'
  compile 'org.mockito:mockito-core'
  compile 'org.spockframework:spock-core'
}
"""
    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("2.6.0")
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.1")
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.1-groovy-2.4-rc-2")
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test multi-project upgrade"(String plugin) {
    given:
    test.gdmcJsonFile << PRE_SET_DEPENDENCIES
    setupMultiProject(test, plugin, [
        mockitoVersion: ':2.6.0',
        chopVersion: ':0.1.7.1',
        spockVersion: ':1.1-groovy-2.4-rc-2'])

    when:
    def result = test.build("gdmcUpgrade")

    then:
    result.task(":javalib:gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcUpgrade").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("2.6.0")
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.1")
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.1-groovy-2.4-rc-2")
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test single-project upgradeAll"(String plugin) {
    given:
    test.gdmcJsonFile << PRE_SET_DEPENDENCIES
    test.gradleBuildFile << buildFilePrefix(plugin)

    when:
    def result = test.build("gdmcUpgradeAll")

    then:
    result.task(":gdmcUpgradeAll").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("2.6.0")
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.1")
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.1-groovy-2.4-rc-2")
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test multi-project upgradeAll"(String plugin) {
    given:
    test.gdmcJsonFile << PRE_SET_DEPENDENCIES
    test.gradleBuildFile << """
allprojects {
  group = "com.example"
  version = "0.0.1-SNAPSHOT"
  
  repositories {
    jcenter()
  }
}
"""
    test.subProject("javalib").with {
      gradleBuildFile << """
plugins {
  id 'java'
${plugin}
}

gdmcLogger {
 enable()
}
"""
    }
    test.subProject("groovylib").with {
      gradleBuildFile << """
plugins {
  id 'groovy'
${plugin}
}

dependencies {
   compile project(':javalib')
}
"""
    }

    when:
    def result = test.build("gdmcUpgradeAll")

    then:
    result.task(":javalib:gdmcUpgradeAll").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcUpgradeAll").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("2.6.0")
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.1")
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("1.1-groovy-2.4-rc-2")
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
