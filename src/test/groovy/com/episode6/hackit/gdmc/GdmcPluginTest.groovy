package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.testutil.IntegrationTest
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

/**
 * Tests {@link GdmcPlugin}
 */
class GdmcPluginTest extends Specification {

  private static final CHOP_IMPORT = "import com.episode6.hackit.chop.Chop;"

  @Rule final IntegrationTest test = new IntegrationTest()

  def setup() {
    test.createJavaFile(packageName: "com.episode6.testproject", imports: CHOP_IMPORT)
    test.createJavaFile(packageName: "com.episode6.testproject", className: "SampleClassTest", dir: "test")
  }

  def "test resolve pre-set dependencies"() {
    given:
    test.gdmcJsonFile << """
{
  "chop-android": {
    "alias": [
      "com.episode6.hackit.chop:chop-core",
      "com.episode6.hackit.chop:chop-android"
    ]
  },
  "chop-all": {
    "alias": "chop-android"
  },
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.2"
   },
   "com.episode6.hackit.chop:chop-android": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-android",
      "version": "0.1.7.2"
   },
   "org.spockframework:spock-core": {
     "groupId": "org.spockframework",
     "artifactId": "spock-core",
     "version": "1.1-groovy-2.4-rc-2"
   }
}
"""
    test.gradleBuildFile << """
plugins {
  id 'groovy'
  id 'com.episode6.hackit.gdmc'
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
}

dependencies {
   compile gdmc('chop-all')
   testCompile(gdmc(group: 'org.spockframework', name: 'spock-core'))  {
    exclude module: 'groovy-all'
  }
}
"""

    when:
    def result = test.runTask("build")

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS
  }

}
