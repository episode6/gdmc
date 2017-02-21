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
    String version = opts.version ?: '0.0.1-SNAPSHOT'
    return """
  "${packageName}:${name}": {
    "groupId": "${packageName}",
    "artifactId": "${name}",
    "version": "${version}"
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
    test.gradleBuildFile << buildFilePrefix(plugin)

    when:
    def result = test.build("gdmcImportSelf")

    then:
    result.task(":gdmcImportSelf").outcome == TaskOutcome.SUCCESS
    test.gdmcJsonFile.exists()
    with(test.gdmcJsonFile.asJson()) {
      with(get("com.example.testproject:sample-proj")) {
        groupId == "com.example.testproject"
        artifactId == "sample-proj"
        version == "0.0.1-SNAPSHOT"
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
    setupMultiProject(test, plugin)

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
        version == "0.0.1-SNAPSHOT"
      }
      with(get("com.example:groovylib")) {
        groupId == "com.example"
        artifactId == "groovylib"
        version == "0.0.1-SNAPSHOT"
      }
      size() == 2
    }

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "test validateSelf single-project success"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1"])
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

  def "test validateSelf single-project fail"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2"])
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
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2"])

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

  def "test validateSelf single-project skip"(String plugin) {
    given:
    test.name = "sample-proj"
    test.gradleBuildFile << buildFilePrefix(plugin)
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
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.1"])

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
    setupMultiProject(test, plugin, [projectVersion: '0.0.1'])
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
    setupMultiProject(test, plugin, [projectVersion: '0.0.2'])
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
    setupMultiProject(test, plugin)
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
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2"])

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
    test.gradleBuildFile << buildFilePrefix(plugin, [version: "0.0.2"])

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