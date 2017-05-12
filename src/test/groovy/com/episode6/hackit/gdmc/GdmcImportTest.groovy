package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests the gdmcImport task.
 */
class GdmcImportTest extends Specification {

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

  static final String GDMC_DEPLOYABLE_CONTENTS = """
{
  "com.episode6.hackit.deployable:deployable": {
      "groupId": "com.episode6.hackit.deployable",
      "artifactId": "deployable",
      "version": "0.1.2"
   }
}
"""

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test import from nothing"(String plugin) {
    given:
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

  def "test import buildscript from nothing"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefixWithBuildscript(plugin)
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
    def result = test.build("gdmcImportBuildscript")

    then:
    result.task(":gdmcImportBuildscript").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.deployable:deployable")) {
        groupId == "com.episode6.hackit.deployable"
        artifactId == "deployable"
        version == "0.1.2"
      }
      if (plugin == GDMC_SPRINGS_COMPAT_PLUGIN) {
        size() == 2
        verifySpringPlugin(delegate)
      } else {
        size() == 1
      }
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test import doesnt overwrite existing gdmc"(String plugin) {
    given:
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

  def "test import buildscript doesnt overwrite existing gdmc"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefixWithBuildscript(plugin, [deployableVersion: "0.1.4"])
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
   compile 'org.mockito:mockito-core:2.7.1'
   
   testCompile(group: 'org.spockframework', name: 'spock-core', version: '1.1-groovy-2.4-rc-3')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << GDMC_DEPLOYABLE_CONTENTS

    when:
    def result = test.build("gdmcImportBuildscript")

    then:
    result.task(":gdmcImportBuildscript").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.deployable:deployable")) {
        groupId == "com.episode6.hackit.deployable"
        artifactId == "deployable"
        version == "0.1.2"
      }
      if (plugin == GDMC_SPRINGS_COMPAT_PLUGIN) {
        size() == 2
        verifySpringPlugin(delegate)
      } else {
        size() == 1
      }
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test import does overwrite existing gdmc when told"(String plugin) {
    given:
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
    def result = test.build("-Pgdmc.overwrite=true", "gdmcImport")

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

  def "test import buildscript does overwrite existing gdmc when told"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefixWithBuildscript(plugin, [deployableVersion: "0.1.4"])
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
   compile 'org.mockito:mockito-core:2.7.1'
   
   testCompile(group: 'org.spockframework', name: 'spock-core', version: '1.1-groovy-2.4-rc-3')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << GDMC_DEPLOYABLE_CONTENTS

    when:
    def result = test.build("-Pgdmc.overwrite=true", "gdmcImportBuildscript")

    then:
    result.task(":gdmcImportBuildscript").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.episode6.hackit.deployable:deployable")) {
        groupId == "com.episode6.hackit.deployable"
        artifactId == "deployable"
        version == "0.1.4"
      }
      if (plugin == GDMC_SPRINGS_COMPAT_PLUGIN) {
        size() == 2
        verifySpringPlugin(delegate)
      } else {
        size() == 1
      }
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test import from nothing ignores overrides"(String plugin) {
    given:
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
    test.singleGdmcOverrideFile() << GDMC_CONTENTS

    when:
    def result = test.build("-Pgdmc.overwrite=true", "gdmcImport")

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

  def "test import buildscript from multi-project"(String plugin) {
    given:
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
    def result = test.build("gdmcImportBuildscript")

    then:
    result.task(":javalib:gdmcImportBuildscript").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcImportBuildscript").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      if (plugin == GDMC_SPRINGS_COMPAT_PLUGIN) {
        size() == 2
        verifySpringPlugin(delegate)
      } else {
        size() == 1
      }
      with(get("com.episode6.hackit.deployable:deployable")) {
        groupId == "com.episode6.hackit.deployable"
        artifactId == "deployable"
        version == "0.1.2"
      }
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
    given:
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
    given:
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

  def "test importTransitive pulls in transitive deps even if first order dep is mapped"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core:0.1.7.2'
}
"""
    test.gdmcJsonFile << """
{
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.2"
   },
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

  def "test importTransitive pulls in transitive deps even if first order dep is mapped alias"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile gdmc('testalias')
}
"""
    test.gdmcJsonFile << """
{
  "testalias": {
      "alias": [
        "com.episode6.hackit.chop:chop-core",
        "junit:junit"
      ]
  },
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.2"
   },
   "junit:junit": {
      "groupId": "junit",
      "artifactId": "junit",
      "version": "4.12"
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
        version == "1.3.7"
      }
      with(get("junit:junit")) {
        groupId == "junit"
        artifactId == "junit"
        version == "4.12"
      }
      with(get("org.hamcrest:hamcrest-core")) {
        groupId == "org.hamcrest"
        artifactId == "hamcrest-core"
        version == "1.3"
      }
      with(get("testalias")) {
        alias.size() == 2
      }
      size() == 5
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importTransitive pulls from mapped dependency version, not resolved/upgraded version"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'junit:junit'
}
"""
    test.gdmcJsonFile << """
{
   "junit:junit": {
      "groupId": "junit",
      "artifactId": "junit",
      "version": "4.9"
   }
}
"""
    when:
    def result = test.build("gdmcImportTransitive")

    then:
    result.task(":gdmcImportTransitive").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()

    // upgraded versions of junit include an updated hamcrest dependency.
    // here we want to ensure we old hamcrest 1.1, which junit 4.9 depended on
    with(test.gdmcJsonFile.asJson()) {
      with(get("junit:junit")) {
        groupId == "junit"
        artifactId == "junit"
        version == "4.9"
      }
      with(get("org.hamcrest:hamcrest-core")) {
        groupId == "org.hamcrest"
        artifactId == "hamcrest-core"
        version == "1.1"
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test import ignores unmapped dependencies"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'junit:junit'
}
"""
    when:
    def result = test.build("gdmcImport")

    then:
    result.task(":gdmcImport").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.asJson().size() == 0

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importTransitive ignores unmapped dependencies"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'junit:junit'
}
"""
    when:
    def result = test.build("gdmcImportTransitive")

    then:
    result.task(":gdmcImportTransitive").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.asJson().size() == 0

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importTransitive does overwrite existing gdmc when told"(String plugin) {
    given:
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
    def result = test.build("-Pgdmc.overwrite=true", "gdmcImportTransitive")

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

  private static boolean verifySpringPlugin(Object json) {
    json.get("io.spring.gradle:dependency-management-plugin").with {
      assert groupId == "io.spring.gradle"
      assert artifactId == "dependency-management-plugin"
      assert version == "1.0.0.RELEASE"
    }
    return true;
  }
}
