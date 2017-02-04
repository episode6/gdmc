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
   },
   "org.spockframework:spock-core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.1-groovy-2.4-rc-2"
   }
}
"""

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
    def result = test.build("gdmcImport")

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

  def "test import doesnt overwrite existing gdmc"(String plugin) {
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
   compile 'org.mockito:mockito-core:2.7.1'
   
   testCompile(group: 'org.spockframework', name: 'spock-core', version: '1.1-groovy-2.4-rc-3')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << GDMC_CONTENTS

    when:
    def result = test.build("gdmcImport")

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
        version == "0.1.7.1"
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

  def "test import does overwrite existing gdmc when told"(String plugin) {
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
   compile 'org.mockito:mockito-core:2.7.1'
   
   testCompile(group: 'org.spockframework', name: 'spock-core', version: '1.1-groovy-2.4-rc-3')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << GDMC_CONTENTS

    when:
    def result = test.build("-Poverwrite=true", "gdmcImport")

    then:
    result.task(":gdmcImport").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("org.mockito:mockito-core")) {
        groupId == "org.mockito"
        artifactId == "mockito-core"
        version == "2.7.1"
      }
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        version == "0.1.7.2"
      }
      with(get("org.spockframework:spock-core")) {
        groupId == "org.spockframework"
        artifactId == "spock-core"
        version == "1.1-groovy-2.4-rc-3"
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
    def result = test.build("gdmcImport")

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



  def "test importTransitive from nothing"(String plugin) {
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
}
"""
    when:
    def result = test.build("gdmcImportTransitive")

    then:
    result.task(":gdmcImportTransitive").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        version == "0.1.7.2"
      }
      with(get("net.sourceforge.findbugs:jsr305")) {
        groupId == "net.sourceforge.findbugs"
        artifactId == "jsr305"
        version == "1.3.7"
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importTransitive doesnt overwrite existing gdmc"(String plugin) {
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
}
"""
    test.gdmcJsonFile << """
{
  "net.sourceforge.findbugs:jsr305": {
      "groupId": "net.sourceforge.findbugs",
      "artifactId": "jsr305",
      "version": "1.3.2"
   }
}
"""
    when:
    def result = test.build("gdmcImportTransitive")

    then:
    result.task(":gdmcImportTransitive").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        version == "0.1.7.2"
      }
      with(get("net.sourceforge.findbugs:jsr305")) {
        groupId == "net.sourceforge.findbugs"
        artifactId == "jsr305"
        version == "1.3.2"
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  /**
   * importTransitive can't overwrite transitive dependencies that already exist when using
   * springs's dependency management plugin. It's a bummer but not blocking on it now.
   */
  def "test importTransitive does overwrite existing gdmc when told"(String plugin) {
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
}
"""
    test.gdmcJsonFile << """
{
  "net.sourceforge.findbugs:jsr305": {
      "groupId": "net.sourceforge.findbugs",
      "artifactId": "jsr305",
      "version": "1.3.2"
   }
}
"""
    when:
    def result = test.build("-Poverwrite=true", "gdmcImportTransitive")

    then:
    result.task(":gdmcImportTransitive").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.chop:chop-core")) {
        groupId == "com.episode6.hackit.chop"
        artifactId == "chop-core"
        version == "0.1.7.2"
      }
      with(get("net.sourceforge.findbugs:jsr305")) {
        groupId == "net.sourceforge.findbugs"
        artifactId == "jsr305"
        version == "1.3.7"
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    // purposefully not testing GDMC_SPRINGS_COMPAT_PLUGIN here
  }
}
