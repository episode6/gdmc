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
     "version": "2.7.0"
   },
   "org.spockframework:spock-core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.1-groovy-2.4-rc-2"
   }
"""

  @Rule final IntegrationTest test = new IntegrationTest()


  def "test resolve pre-set dependencies simple"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core'
   testCompile(group: 'org.spockframework', name: 'spock-core')  {
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

  def "test resolve pre-set dependencies aliases"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.createJavaFile(packageName: "com.episode6.testproject", imports: MOCKITO_IMPORT, className: "SampleClass2")
    test.gradleBuildFile << """
dependencies {
   compile gdmc('subgroup')
   testCompile(group: 'org.spockframework', name: 'spock-core')  {
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

  def "test failure on missing dependency"(String plugin) {
    given:
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core'
}
"""
    when:
    def result = test.buildAndFail("build")

    then:
    result.output.contains(plugin == GDMC_PLUGIN ?
        "Unmapped dependency found: com.episode6.hackit.chop:chop-core" :
        "Could not resolve all dependencies for configuration")

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
    result.output.contains("Could not resolve all dependencies")

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
   compile 'com.episode6.hackit.chop:chop-core'
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
