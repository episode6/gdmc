package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import com.episode6.hackit.gdmc.testutil.MavenOutputVerifier
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Test using gdmc with deployable
 */
class GdmcDeployableTest extends Specification {

  private static String deployableBuildGradle(String plugin, Map opts = [:]) {
    return """
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'com.episode6.hackit.deployable:deployable:0.1.2'
  }
}

plugins {
id 'java'
${plugin}
}

apply plugin: 'com.episode6.hackit.deployable.jar'

deployable {
  pom {
    description "Test POM Description"
    url "https://pom_url.com"

    scm {
      url "extensible"
      connection "scm:https://scm_connection.com"
      developerConnection "scm:https://scm_dev_connection.com"
    }
    license {
      name "The MIT License (MIT)"
      url "https://license.com"
      distribution "repo"
    }
    developer {
      id "DeveloperId"
      name "DeveloperName"
    }
  }

  nexus {
    username "nexusUsername"
    password "nexusPassword"
    snapshotRepoUrl "file://localhost${opts.repoFile?.absolutePath}"
    releaseRepoUrl "file://localhost${opts.repoFile?.absolutePath}"
  }
}

gdmcLogger {
 enable()
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
}

dependencies {
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
}
"""
  }

  static final String GDMC_CONTENTS = """
{
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.1"
   },
   "org.mockito:mockito-core": {
     "groupId": "org.mockito",
     "artifactId": "mockito-core",
     "version": "2.7.0"
   }
}
"""

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test deployable library"(String plugin) {
    given:
    test.name = "javalib"
    File snapshotRepo = test.newFolder("build", "m2", "snapshot")
    test.gradleBuildFile << deployableBuildGradle(plugin, [repoFile: snapshotRepo])
    test.gdmcJsonFile << GDMC_CONTENTS
    test.createJavaFile(packageName: "com.example.testproject", imports: CHOP_IMPORT)
    MavenOutputVerifier mavenOutputVerifier = new MavenOutputVerifier(
        repo: snapshotRepo,
        groupId: "com.example.testproject",
        artifactId: "javalib",
        versionName: "0.0.1-SNAPSHOT")

    when:
    def result = test.build("deploy")

    then:
    result.task(":uploadArchives").outcome == TaskOutcome.SUCCESS
    mavenOutputVerifier.verifyStandardOutput()
    mavenOutputVerifier.verifyPomDependency("com.episode6.hackit.chop", "chop-core", "0.1.7.1")
    mavenOutputVerifier.verifyPomDependency("org.mockito", "mockito-core", "2.7.0")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
