package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.GradleTestProject
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
    String deps = opts.deps ?: """
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
"""
    return """
buildscript {
  repositories {
    jcenter()
  }
  
}

plugins {
id 'java'
id 'com.episode6.hackit.deployable.jar'
${plugin}
}


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
${deps}
}
"""
  }

  static final String GDMC_CONTENTS = """
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
"""

  @Rule final IntegrationTest test = new IntegrationTest()

  private MavenOutputVerifier commonSetup(Map opts = [:]) {
    GradleTestProject testProject = opts.testProject ?: test
    String packageName = opts.packageName ?: "com.example.testproject"
    String version = opts.version ?: "0.0.1-SNAPSHOT"
    File repoFile = opts.repoFile

    testProject.createJavaFile(packageName: packageName, imports: CHOP_IMPORT)
    testProject.createJavaFile(packageName: packageName, className: "SampleClass2", imports: MOCKITO_IMPORT)
    return new MavenOutputVerifier(
        repo: repoFile,
        groupId: packageName,
        artifactId: testProject.name,
        versionName: version)
  }

  def "test deployable library"(String plugin) {
    given:
    test.name = "javalib"
    File snapshotRepo = test.newFolder("build", "m2", "snapshot")
    MavenOutputVerifier mavenOutputVerifier = commonSetup(repoFile: snapshotRepo)
    test.gradleBuildFile << deployableBuildGradle(plugin, [repoFile: snapshotRepo])
    test.gdmcJsonFile << "{${GDMC_CONTENTS}}"

    when:
    def result = test.build("deploy")

    then:
    result.task(":uploadArchives").outcome == TaskOutcome.SUCCESS
    commonVerify(mavenOutputVerifier)

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test deployable library with aliases"(String plugin) {
    given:
    test.name = "javalib"
    File snapshotRepo = test.newFolder("build", "m2", "snapshot")
    MavenOutputVerifier mavenOutputVerifier = commonSetup(repoFile: snapshotRepo)
    test.gradleBuildFile << deployableBuildGradle(plugin, [
        repoFile: snapshotRepo,
        deps: "compile gdmc('myAlias')"])
    test.gdmcJsonFile << """
{
  "myAlias": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "org.mockito:mockito-core"]
  },
${GDMC_CONTENTS}
}
"""
    when:
    def result = test.build("deploy")

    then:
    result.task(":uploadArchives").outcome == TaskOutcome.SUCCESS
    commonVerify(mavenOutputVerifier)

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test deployable library with namespaced aliases"(String plugin) {
    given:
    test.name = "javalib"
    File snapshotRepo = test.newFolder("build", "m2", "snapshot")
    MavenOutputVerifier mavenOutputVerifier = commonSetup(repoFile: snapshotRepo)
    test.gradleBuildFile << deployableBuildGradle(plugin, [
        repoFile: snapshotRepo,
        deps: "compile 'namespaced:alias'"])
    test.gdmcJsonFile << """
{
  "namespaced:alias": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "org.mockito:mockito-core"]
  },
${GDMC_CONTENTS}
}
"""
    when:
    def result = test.build("deploy")

    then:
    result.task(":uploadArchives").outcome == TaskOutcome.SUCCESS
    commonVerify(mavenOutputVerifier)

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  private static boolean commonVerify(MavenOutputVerifier mavenOutputVerifier) {
    assert mavenOutputVerifier.verifyStandardOutput()
    assert mavenOutputVerifier.verifyPomDependency(
        groupId: "com.episode6.hackit.chop",
        artifactId:  "chop-core",
        version:  "0.1.7.1",
        times: 1)
    assert mavenOutputVerifier.verifyPomDependency(
        groupId: "org.mockito",
        artifactId:  "mockito-core",
        version:  "2.7.0",
        times: 1)
    return true
  }

}
