package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests compatibility with android plugin
 */
class GdmcAndroidTest extends Specification {

  private static String androidBuildGradle(String plugin) {
    return """
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:2.2.3'
  }
}

plugins {
${plugin}
}

apply plugin: 'com.android.library'

gdmcLogger {
 enable()
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
}

android {
  compileSdkVersion 19
  buildToolsVersion "25.0.2"
}

dependencies {
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
}
"""
  }

  private static String simpleManifest() {
    return """
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.testproject">
    <application>
    </application>
</manifest>
"""
  }

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test resolve missing dependencies android plugin"(String plugin) {
    given:
    test.gdmcJsonFile << "{}"
    test.gradleBuildFile << androidBuildGradle(plugin)
    test.newFolder("src", "main").newFile("AndroidManifest.xml") << simpleManifest()

    when:
    def result = test.build("-Pgdmc.forceResolve=true", "gdmcResolve")

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
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
