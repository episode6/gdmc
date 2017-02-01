package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

/**
 * Tests {@link GdmcPlugin}
 */
class GdmcBuildTest extends Specification {
  private static final String CHOP_IMPORT = """
import com.episode6.hackit.chop.Chop;

"""
  private static final String SPOCK_IMPORT = """
import spock.lang.Specification;

"""
  private static final String MOCKITO_IMPORT = """
import org.mockito.Mockito;

"""
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
  private static final String buildFilePrefix(String plugin) {
    """
plugins {
  id 'groovy'
  id '${plugin}'
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
}

"""
  }

  @Rule final IntegrationTest test = new IntegrationTest()

  def setup() {
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test", imports: SPOCK_IMPORT)
  }


  def "test resolve pre-set dependencies simple"(String plugin) {
    given:
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
    def result = test.runTask("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    "com.episode6.hackit.gdmc"  | _
  }


  def "test resolve pre-set dependencies aliases"(String plugin) {
    given:
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
    def result = test.runTask("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS

    where:
    plugin                      | _
    "com.episode6.hackit.gdmc"  | _
  }



  def "test failure on missing dependency"(String plugin) {
    given:
    test.gradleBuildFile << buildFilePrefix(plugin)
    test.gradleBuildFile << """
dependencies {
   compile 'com.episode6.hackit.chop:chop-core'
}
"""
    when:
    def result = test.runTaskAndFail("build")

    then:
    result.output.contains("Unmapped dependency found: com.episode6.hackit.chop:chop-core")

    where:
    plugin                      | _
    "com.episode6.hackit.gdmc"  | _
  }
}
