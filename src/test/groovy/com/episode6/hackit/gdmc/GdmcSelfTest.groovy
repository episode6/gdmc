package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests the importSelf and validateSelf tasks
 */
class GdmcSelfTest extends Specification {

  private static final String singleProjectGdmc(Map opts = [:]) {
    String packageName = opts.packageName ?: 'com.example.testproject'
    String name = opts.name ?: 'sample-proj'
    String version = opts.version ?: '0.0.1'
    String versionString = opts.inheritVersion != null ?
        "\"inheritVersion\": \"${opts.inheritVersion}\"" :
        "\"version\": \"${version}\""
    return """
  "${packageName}:${name}": {
    "groupId": "${packageName}",
    "artifactId": "${name}",
    ${versionString}
  }
"""
  }

  private static final String multiProjectGdmc(String version) {
    """
{
${singleProjectGdmc(packageName: "com.example", name: "javalib", version: version)},
${singleProjectGdmc(packageName: "com.example", name: "groovylib", version: version)}
}
"""
  }

  @Rule final IntegrationTest test = new IntegrationTest()

  def "test importSelf single-project"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1", includeMaven: true])

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example.testproject:sample-proj")) {
        groupId == "com.example.testproject"
        artifactId == "sample-proj"
        version == "0.0.1"
      }
      size() == 1
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importSelf single-project skip without maven"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1"])

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    !test.gdmcJsonFile.exists()

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importSelf single-project ignores overrides"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1", includeMaven: true])
    File gdmcOverridFile = test.singleGdmcOverrideFile()
    gdmcOverridFile << "{${singleProjectGdmc(version: "0.2")}}"

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example.testproject:sample-proj")) {
        groupId == "com.example.testproject"
        artifactId == "sample-proj"
        version == "0.0.1"
      }
      size() == 1
    }
    with(gdmcOverridFile.asJson()) {
      with(get("com.example.testproject:sample-proj")) {
        groupId == "com.example.testproject"
        artifactId == "sample-proj"
        version == "0.2"
      }
      size() == 1
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importSelf multi-project"(String plugin) {
    given:
    setupMultiProject(test, plugin, [projectVersion: "0.1", includeMaven: true])

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":javalib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example:javalib")) {
        groupId == "com.example"
        artifactId == "javalib"
        version == "0.1"
      }
      with(get("com.example:groovylib")) {
        groupId == "com.example"
        artifactId == "groovylib"
        version == "0.1"
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importSelf multi-project ignore inheritance"(String plugin) {
    given:
    setupMultiProject(test, plugin, [projectVersion: "0.2", includeMaven: true])
    test.gdmcJsonFile << """
{
${singleProjectGdmc(packageName: "com.example", name: "javalib", version: "0.1")},
${singleProjectGdmc(packageName: "com.example", name: "groovylib", inheritVersion: "com.example:javalib")}
}
"""

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":javalib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example:javalib")) {
        groupId == "com.example"
        artifactId == "javalib"
        version == "0.2"
      }
      with(get("com.example:groovylib")) {
        groupId == "com.example"
        artifactId == "groovylib"
        inheritVersion == "com.example:javalib"
        version == null
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importSelf multi-project excludes if validate not required"(String plugin) {
    given:
    setupMultiProject(test, plugin, [projectVersion: "0.1", includeMaven: true])
    new File(new File(test.root, "groovylib"), "build.gradle") << """

gdmcValidateSelf {
  required = {false}
}
"""

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":javalib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example:javalib")) {
        groupId == "com.example"
        artifactId == "javalib"
        version == "0.1"
      }
      size() == 1
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project success"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1", includeMaven: true])
    test.gdmcJsonFile << "{${singleProjectGdmc(version: "0.0.1")}}"

    when:
    def result = test.build("gdmcValidateSelf")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project success with inheritVersion"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1", includeMaven: true])
    test.gdmcJsonFile << """
{
${singleProjectGdmc(packageName: "com.example.testproject", name: "otherlib", version: "0.0.1")},
${singleProjectGdmc(packageName: "com.example.testproject", name: "sample-proj", inheritVersion: "com.example.testproject:otherlib")}
}
"""

    when:
    def result = test.build("gdmcValidateSelf")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project success thanks to overrides"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1", includeMaven: true])
    test.singleGdmcOverrideFile() << "{${singleProjectGdmc(version: "0.0.1")}}"

    when:
    def result = test.build("gdmcValidateSelf")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project fail"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2", includeMaven: true])
    test.gdmcJsonFile << "{${singleProjectGdmc(version: "0.0.1")}}"

    when:
    def result = test.buildAndFail("gdmcValidateSelf")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.FAILED
    result.output.contains("Failed to validate project com.example.testproject:sample-proj:0.0.2")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project fail on empty"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2", includeMaven: true])

    when:
    def result = test.buildAndFail("gdmcValidateSelf")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.FAILED
    result.output.contains("Failed to validate project com.example.testproject:sample-proj:0.0.2")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project skip for snapshot"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1-SNAPSHOT", includeMaven: true])
    test.gdmcJsonFile << "{${singleProjectGdmc(version: "0.0.2")}}"

    when:
    def result = test.build("gdmcValidateSelf")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project skip witout maven"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.3"])
    test.gdmcJsonFile << "{${singleProjectGdmc(version: "0.0.2")}}"

    when:
    def result = test.build("gdmcValidateSelf")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test importSelf then validateSelf"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1", includeMaven: true])

    when:
    def result1 = test.build("gdmcImportSelf")
    def result2 = test.build("gdmcValidateSelf")

    then:
    result1.task(":gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    result2.task(":gdmcValidateSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example.testproject:sample-proj")) {
        groupId == "com.example.testproject"
        artifactId == "sample-proj"
        version == "0.0.1"
      }
      size() == 1
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf multi-project success"(String plugin) {
    given:
    setupMultiProject(test, plugin, [projectVersion: '0.0.1', includeMaven: true])
    test.gdmcJsonFile << multiProjectGdmc("0.0.1")

    when:
    def result = test.build("gdmcValidateSelf")

    then:
    result.task(":javalib:gdmcValidateSelf").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcValidateSelf").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf multi-project fail"(String plugin) {
    given:
    setupMultiProject(test, plugin, [projectVersion: '0.0.2', includeMaven: true])
    test.gdmcJsonFile << multiProjectGdmc("0.0.1")

    when:
    def result = test.buildAndFail("gdmcValidateSelf")

    then:
    result.task(":javalib:gdmcValidateSelf")?.outcome == TaskOutcome.FAILED ||
        result.task(":groovylib:gdmcValidateSelf")?.outcome == TaskOutcome.FAILED
    result.output.contains("Failed to validate project com.example:")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf multi-project skip"(String plugin) {
    given:
    setupMultiProject(test, plugin, [includeMaven: true])
    test.gdmcJsonFile << multiProjectGdmc("0.0.2")

    when:
    def result = test.build("gdmcValidateSelf")

    then:
    result.task(":javalib:gdmcValidateSelf").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:gdmcValidateSelf").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf fail on check"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2", includeMaven: true])

    when:
    def result = test.buildAndFail("check")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.FAILED
    result.output.contains("Failed to validate project com.example.testproject:sample-proj:0.0.2")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf fail on test"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2", includeMaven: true])

    when:
    def result = test.buildAndFail("test")

    then:
    result.task(":gdmcValidateSelf").outcome == TaskOutcome.FAILED
    result.output.contains("Failed to validate project com.example.testproject:sample-proj:0.0.2")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
