package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

/**
 * Tests the gdmcResolve task
 */
class GdmcResolveTest extends Specification {

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test resolve missing dependencies"() {
    given:
    test.gdmcJsonFile << "{}"
    test.gradleBuildFile << """
plugins {
  id 'groovy'
  id 'com.episode6.hackit.gdmc'
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
   compile gdmc('org.mockito:mockito-core')
   compile gdmc('com.episode6.hackit.chop:chop-core')
   testCompile(gdmc(group: 'org.spockframework', name: 'spock-core'))  {
    exclude module: 'groovy-all'
  }
}
"""

    when:
    def result = test.runTask("gdmcResolve")

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
      verifyJsonSortOrder((Map)delegate)
    }
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
