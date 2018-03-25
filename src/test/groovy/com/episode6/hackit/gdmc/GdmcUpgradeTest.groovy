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

  private static final String LOCKED_DEPENDENCIES = """
{
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.1"
   },
   "org.mockito:mockito-core": {
     "groupId": "org.mockito",
     "artifactId": "mockito-core",
     "version": "2.6.0",
     "locked": true
   },
   "javax.inject:javax.inject": {
     "groupId": "javax.inject",
     "artifactId": "javax.inject",
     "version": "1",
     "locked": true
   },
  "android.compilesdk": {
    "version": "25",
    "locked": true
  },
  "android.buildtools": {
    "version": "26.0.0",
    "locked": true
  }
}
"""

  private static final String DEPENDENCIES_WITH_SELF = """
{
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.1"
   },
   "com.example:groovylib": {
      "groupId": "com.example",
      "artifactId": "groovylib",
      "version": "1.0"
   },
   "com.example:javalib": {
      "groupId": "com.example",
      "artifactId": "javalib",
      "version": "1.0"
   },
   "org.mockito:mockito-core": {
     "groupId": "org.mockito",
     "artifactId": "mockito-core",
     "version": "2.6.0"
   },
   "org.spockframework:spock-core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.0-groovy-2.4"
   }
}
"""

  static final String GDMC_DEPLOYABLE_CONTENTS = """
{
  "com.episode6.hackit.deployable:deployable": {
      "groupId": "com.episode6.hackit.deployable",
      "artifactId": "deployable",
      "version": "0.1.2"
   }
}
"""

  static String buildFilePrefixWithBuildscript(String plugins, Map opts = [:]) {
    return """
buildscript {
  repositories {
    maven {url "https://oss.sonatype.org/content/repositories/snapshots/"}
    jcenter()
  }
  dependencies {
    classpath 'com.episode6.hackit.deployable:deployable:${opts.deployableVersion ?: '0.1.2'}'
  }
}
${buildFilePrefix(plugins, opts)}
"""
  }

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test single-project upgrade"(String plugin) {
    given:
    test.gdmcJsonFile << PRE_SET_DEPENDENCIES
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
  implementation 'com.episode6.hackit.chop:chop-core'
  implementation 'org.mockito:mockito-core'
  implementation 'org.spockframework:spock-core'
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
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test upgradeBuildscript doesnt affect unmapped deps"(String plugin) {
    given:
    test.gdmcJsonFile << "{}"
    test.gradleBuildFile << buildFilePrefixWithBuildscript(plugin)
    test.gradleBuildFile << """
dependencies {
  implementation 'com.episode6.hackit.chop:chop-core'
  implementation 'org.mockito:mockito-core'
  implementation 'org.spockframework:spock-core'
}
"""
    when:
    def result = test.build("gdmcUpgradeBuildscript")

    then:
    result.task(":gdmcUpgradeBuildscript").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      size() == 0
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test upgradeBuildscript"(String plugin) {
    given:
    test.gdmcJsonFile << GDMC_DEPLOYABLE_CONTENTS
    test.gradleBuildFile << buildFilePrefixWithBuildscript(plugin)
    test.gradleBuildFile << """
dependencies {
  implementation 'com.episode6.hackit.chop:chop-core'
  implementation 'org.mockito:mockito-core'
  implementation 'org.spockframework:spock-core'
}
"""
    when:
    def result = test.build("gdmcUpgradeBuildscript")

    then:
    result.task(":gdmcUpgradeBuildscript").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.deployable:deployable")) {
        groupId == "com.episode6.hackit.deployable"
        artifactId == "deployable"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.2")
      }
      size() == 1
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test upgradeBuildscript multi-project"(String plugin) {
    given:
    test.gdmcJsonFile << GDMC_DEPLOYABLE_CONTENTS
    test.gradleBuildFile << """
buildscript {
  repositories {
    maven {url "https://oss.sonatype.org/content/repositories/snapshots/"}
    jcenter()
  }
  dependencies {
    classpath 'com.episode6.hackit.deployable:deployable:0.1.2'
  }
}
"""
    setupMultiProject(test, plugin)

    when:
    def result = test.build("gdmcUpgradeBuildscript")

    then:
    result.task(":javalib:gdmcUpgradeBuildscript").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcUpgradeBuildscript").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.deployable:deployable")) {
        groupId == "com.episode6.hackit.deployable"
        artifactId == "deployable"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.2")
      }
      size() == 1
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test locked upgrade"(String plugin) {
    given:
    test.gdmcJsonFile << LOCKED_DEPENDENCIES
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
  implementation 'com.episode6.hackit.chop:chop-core'
  implementation 'org.mockito:mockito-core'
  implementation 'javax.inject:javax.inject'
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
        version == "2.6.0"
        locked == true
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("0.1.7.1")
        get("locked") == null
      }
      with(get("javax.inject:javax.inject")) {
        groupId == "javax.inject"
        artifactId == "javax.inject"
        version == "1"
        locked == true
      }
      with(get("android.compilesdk")) {
        version == "25"
        locked == true
      }
      with(get("android.buildtools")) {
        version == "26.0.0"
        locked == true
      }
      size() == 5
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test multi-project upgrade"(String plugin) {
    given:
    test.gdmcJsonFile << PRE_SET_DEPENDENCIES
    setupMultiProject(test, plugin)

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
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
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
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test single-project upgradeAll ignores overrides"(String plugin) {
    given:
    test.gdmcJsonFile << PRE_SET_DEPENDENCIES
    File gdmcOverride = test.singleGdmcOverrideFile()
    gdmcOverride << """
{
  "org.easymock:easymock": {
      "groupId": "org.easymock",
      "artifactId": "easymock",
      "version": "3.3"
  }
}
"""
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
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      size() == 3
    }
    with(gdmcOverride.asJson()) {
      size() == 1
      with(get("org.easymock:easymock")) {
        groupId == "org.easymock"
        artifactId == "easymock"
        version == "3.3"
      }
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
   implementation project(':javalib')
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
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      size() == 3
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test multi-project upgrade excludes self"(String plugin, String task) {
    given:
    test.gdmcJsonFile << DEPENDENCIES_WITH_SELF
    setupMultiProject(test, plugin, [projectVersion: "2.0-SNAPSHOT"])

    when:
    def result = test.build(task)

    then:
    result.task(":javalib:${task}").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:${task}").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        !version.contains("-SNAPSHOT")
        version.asVersion().isGreaterThan("2.6.0")
      }
      with(get("com.example:groovylib")) {
        groupId == "com.example"
        artifactId == "groovylib"
        version == "1.0"
      }
      with(get("com.example:javalib")) {
        groupId == "com.example"
        artifactId == "javalib"
        version == "1.0"
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
        version.asVersion().isGreaterThan("1.0-groovy-2.4")
      }
      size() == 5
    }

    where:
    plugin                      | task
    GDMC_PLUGIN                 | "gdmcUpgrade"
    GDMC_SPRINGS_COMPAT_PLUGIN  | "gdmcUpgrade"
    GDMC_PLUGIN                 | "gdmcUpgradeAll"
    GDMC_SPRINGS_COMPAT_PLUGIN  | "gdmcUpgradeAll"
  }
}
