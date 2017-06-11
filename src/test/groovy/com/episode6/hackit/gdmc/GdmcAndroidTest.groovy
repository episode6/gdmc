package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import com.episode6.hackit.gdmc.testutil.MyDependencyMap
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests compatibility with android plugin
 */
class GdmcAndroidTest extends Specification {

  private static String androidBuildGradle(String plugin, Map opts = [:]) {
    return """
buildscript {
  repositories {
    jcenter()
  }

  // We need to declare this in order to retrieve the android plugin's dependencies
  // I don't really understand why this is.
  dependencies {
    classpath '${MyDependencyMap.lookupDep("com.android.tools.build:gradle")}'
  }
}

plugins {
id 'com.android.library'
${plugin}
}

gdmcLogger {
 enable()
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
}

android {
  compileSdkVersion ${opts.compileSdkVersion ?: '19'}
  buildToolsVersion ${opts.buildToolsVersion ?: '"25.0.2"'} 
}

android {
      lintOptions {
          abortOnError false
      }
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

  private static final String PRE_SET_DEPENDENCIES = """
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.2"
   },
   "org.mockito:mockito-core": {
     "groupId": "org.mockito",
     "artifactId": "mockito-core",
     "version": "2.7.0"
   },
   "org.spockframework:spock-core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.1-groovy-2.4-rc-2"
   }
"""

  private static String ALIAS_DEPS = """
  "mygroup": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "org.mockito:mockito-core",
      "org.spockframework:spock-core"
    ]
  }
"""

  private static final String VERSION_ONLY_DEPS = """
  "android.compilesdk": {
    "version": "25",
    "locked": true
  },
  "android.buildtools": {
    "version": "26.0.0",
    "locked": true
  },
"""

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test android with alias"(String plugin) {
    given:
    test.gdmcJsonFile << """
{
  ${PRE_SET_DEPENDENCIES},
  ${ALIAS_DEPS}
}
"""
    test.gradleBuildFile << androidBuildGradle(plugin)
    test.newFolder("src", "main").newFile("AndroidManifest.xml") << simpleManifest()
    test.gradleBuildFile << """
dependencies {
   compile gdmc('mygroup')
}
"""
    when:
    def result = test.build("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test resolve missing dependencies android plugin"(String plugin) {
    given:
    test.gdmcJsonFile << "{}"
    test.gradleBuildFile << androidBuildGradle(plugin)
    test.newFolder("src", "main").newFile("AndroidManifest.xml") << simpleManifest()
    test.gradleBuildFile << """
dependencies {
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
}
"""
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

  def "test android build using gdmcVersion for buildTools and compileSdk"(String plugin) {
    test.gdmcJsonFile << """
{
  ${PRE_SET_DEPENDENCIES},
  ${VERSION_ONLY_DEPS}
}
"""
    test.gradleBuildFile << androidBuildGradle(plugin, [
        compileSdkVersion: "gdmcVersion('android.compilesdk') as Integer",
        buildToolsVersion: "gdmcVersion('android.buildtools')"
    ])
    test.newFolder("src", "main").newFile("AndroidManifest.xml") << simpleManifest()

    when:
    def result = test.build("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
