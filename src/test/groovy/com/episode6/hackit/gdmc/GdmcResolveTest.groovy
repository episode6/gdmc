package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

/**
 * Tests the gdmcResolveMissing task
 */
class GdmcResolveTest extends Specification {

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test resolve missing dependencies"(String plugin) {
    given:
    test.gdmcJsonFile << "{}"
    test.gradleBuildFile << """
plugins {
  id 'groovy'
  id '${plugin}'
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
  maven {
    url "https://oss.sonatype.org/content/repositories/snapshots/"
  }
}

dependencies {
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
   testCompile(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""

    when:
    def result = test.runTask("gdmcResolveMissing")

    then:
    result.task(":gdmcResolveMissing").outcome == TaskOutcome.SUCCESS
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
    "com.episode6.hackit.gdmc"  | _
  }

  def "mutli-project test"(String plugin) {
    given:
    test.gdmcJsonFile << "{}"
    test.gradleBuildFile << """
allprojects {
  group = "com.example"
  version = "0.0.1-SNAPSHOT"
  
  repositories {
    jcenter()
  }
}
"""
    with(test.subProject("javalib")) {
      gradleBuildFile << """
plugins {
  id 'java'
  id '${plugin}'
}

dependencies {
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
}
"""
    }
    with(test.subProject("groovylib")) {
      gradleBuildFile << """
plugins {
  id 'groovy'
  id '${plugin}'
}
dependencies {
   compile project(':javalib')
   compile 'com.episode6.hackit.chop:chop-core'
   testCompile(group: 'org.spockframework', name: 'spock-core') {
    exclude module: 'groovy-all'
  }
}
"""
    }

    when:
    def result = test.runTask("gdmcResolveMissing")

    then:
    result.task(":javalib:gdmcResolveMissing").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcResolveMissing").outcome == TaskOutcome.SUCCESS
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
    "com.episode6.hackit.gdmc"  | _
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
