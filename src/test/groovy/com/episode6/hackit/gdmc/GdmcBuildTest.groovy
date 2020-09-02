package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import static com.episode6.hackit.gdmc.testutil.TestDefinitions.*

/**
 * Tests building apps using gdmc.
 */
class GdmcBuildTest extends Specification {
  private static final String PRE_SET_DEPENDENCIES = """
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.2"
   },
   "org.mockito:mockito-core": {
     "groupId": "org.mockito",
     "artifactId": "mockito-core",
     "version": "2.7.10"
   },
   "org.spockframework:spock-core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.1-groovy-2.4-rc-2"
   },
"""

  private static String CHOP_DEPENDENCIES_INHERITED_VERSIONS = """
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.9"
   },
   "com.episode6.hackit.chop:chop-junit": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-junit",
      "inheritVersion": "com.episode6.hackit.chop:chop-core"
   }
"""

  @Rule IntegrationTest test = new IntegrationTest()


  def "test resolve pre-set dependencies simple"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   implementation 'com.episode6.hackit.chop:chop-core'
   testImplementation(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << """
{
${PRE_SET_DEPENDENCIES}
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

  def "test resolve pre-set dependencies diff gdmc file location"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   implementation 'com.episode6.hackit.chop:chop-core'
   testImplementation(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.newFolder("gdmcFolder").newFile("new_gdmc_file.json") << """
{
${PRE_SET_DEPENDENCIES}
}
"""
    test.newFile("gradle.properties") << """
gdmc.file=gdmcFolder/new_gdmc_file.json
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

  def "test resolve pre-set dependencies aliases"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKITO_IMPORT, className: "SampleClass2")
    test.gradleBuildFile << """
dependencies {
   implementation gdmc('subgroup')
   testImplementation(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << """
{
  "biggroup": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "mockito"
    ]
  },
  "subgroup": {
    "alias": "biggroup"
  },
  "mockito": {
    "alias": "org.mockito:mockito-core"
  },
${PRE_SET_DEPENDENCIES}
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

  def "test resolve pre-set dependencies with inherited versions"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: CHOP_RULE_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   implementation 'com.episode6.hackit.chop:chop-core'
   testImplementation 'com.episode6.hackit.chop:chop-junit'
}
"""
    test.gdmcJsonFile << """
{
${CHOP_DEPENDENCIES_INHERITED_VERSIONS}
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

  def "test resolve pre-set dependencies aliases from overrides"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKITO_IMPORT, className: "SampleClass2")
    test.gradleBuildFile << """
dependencies {
   implementation gdmc('subgroup')
   testImplementation(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << """
{
${PRE_SET_DEPENDENCIES}
}
"""
    test.singleGdmcOverrideFile() << """
{
  "biggroup": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "mockito"
    ]
  },
  "subgroup": {
    "alias": "biggroup"
  },
  "mockito": {
    "alias": "org.mockito:mockito-core"
  }
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

  def "test resolve pre-set dependencies aliases from split overrides files"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKITO_IMPORT, className: "SampleClass2")
    test.gradleBuildFile << """
dependencies {
   implementation gdmc('subgroup')
   testImplementation(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << """
{
${PRE_SET_DEPENDENCIES}
}
"""
    test.newFile("gradle.properties") << """
gdmc.overrideFiles=overrides_root.json|sub/sub_overrides.json
"""
    test.newFile("overrides_root.json") << """
{
  "biggroup": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "mockito"
    ]
  }
}
"""
    test.newFolder("sub").newFile("sub_overrides.json") << """
{
  "subgroup": {
    "alias": "biggroup"
  },
  "mockito": {
    "alias": "org.mockito:mockito-core"
  }
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

  def "test resolve pre-set dependencies namespaced aliases"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKITO_IMPORT, className: "SampleClass2")
    test.gradleBuildFile << """
dependencies {
   implementation 'testing:subgroup'
   testImplementation(group: 'org.spockframework', name: 'spock-core')  {
    exclude module: 'groovy-all'
  }
}
"""
    test.gdmcJsonFile << """
{
  "biggroup": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "mockito"
    ]
  },
  "testing:subgroup": {
    "alias": "biggroup"
  },
  "mockito": {
    "alias": "org.mockito:mockito-core"
  },
${PRE_SET_DEPENDENCIES}
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

  def "test failure on missing dependency"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   implementation 'com.episode6.hackit.chop:chop-core'
}
"""
    when:
    def result = test.buildAndFail("build")

    then:
    result.output.contains(plugin == GDMC_PLUGIN ?
        "Unmapped dependency found: com.episode6.hackit.chop:chop-core" :
        "Could not resolve all files for configuration")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "mutli-project test failure"(String plugin) {
    given:
    setupMultiProject(test, plugin)

    when:
    def result = test.buildAndFail("build")

    then:
    result.output.contains("Could not resolve all files")

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }

  def "mutli-project test"(String plugin) {
    given:
    setupMultiProject(test, plugin)
    test.gdmcJsonFile << """
{
${PRE_SET_DEPENDENCIES}
}
"""
    when:
    def result = test.build("build")

    then:
    result.task(":javalib:build").outcome == TaskOutcome.SUCCESS
    result.task(":groovylib:build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    GDMC_PLUGIN                 | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
  
  def "test springs-compat override"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    // use invalid chop version in gdmc to ensure it gets overwritten by springs dependency management
    test.gdmcJsonFile << """
{
"com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.0.0.0.0.1"
   }
}
"""
    test.gradleBuildFile << """
dependencyManagement {
  dependencies {
    dependency 'com.episode6.hackit.chop:chop-core:0.1.7.2'
  }
}

dependencies {
   implementation 'com.episode6.hackit.chop:chop-core'
}
"""
    when:
    def result = test.build("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    // only testing with springs_compat
    plugin                      | _
    GDMC_SPRINGS_COMPAT_PLUGIN  | _
  }
}
