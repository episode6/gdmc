package com.episode6.hackit.gdmc

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Tests {@link GdmcPlugin}
 */
class GdmcPluginTest extends Specification {

  @Rule final TemporaryFolder buildFolder = new TemporaryFolder()

  def "test resolve pre-set dependencies"() {
    given:
    buildFolder.newFile("build.gradle") << """
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
    createNonEmptyJavaFile("com.episode6.testproject")
    createNonEmptyJavaFile("com.episode6.testproject", "SampleClassTest", "test")
    File gdmcFolder = buildFolder.newFolder("gdmc")
    File gdmcFile = new File(gdmcFolder, "gdmc.json")
    gdmcFile << """
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

    when:
    def result = GradleRunner.create()
        .withProjectDir(buildFolder.root)
        .withPluginClasspath()
        .withArguments("build")
        .build()

    then:
    result.task(":build").outcome == TaskOutcome.SUCCESS
  }

  def "test resolve missing dependencies"() {
    given:
    buildFolder.newFile("build.gradle") << """
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
   compile gdmc('com.episode6.hackit.chop:chop-core')
   testCompile(gdmc(group: 'org.spockframework', name: 'spock-core'))  {
    exclude module: 'groovy-all'
  }
}
"""
    createNonEmptyJavaFile("com.episode6.testproject")
    createNonEmptyJavaFile("com.episode6.testproject", "SampleClassTest", "test")
    File gdmcFolder = buildFolder.newFolder("gdmc")
    File gdmcFile = new File(gdmcFolder, "gdmc.json")
    gdmcFile << "{}"

    when:
    def result = GradleRunner.create()
        .withProjectDir(buildFolder.root)
        .withPluginClasspath()
        .withArguments("gdmcResolve")
        .buildAndFail()

    then:
    result.output.contains("MISSING DEP:")
  }

  File createNonEmptyJavaFile(
      String packageName,
      String className = "SampleClass",
      String mainDirContainer = "src",
      File rootDir = buildFolder.getRoot()) {
    File dir = rootDir
    "${mainDirContainer}.main.java.${packageName}".tokenize(".").each {
      dir = new File(dir, it)
    }
    dir.mkdirs()
    File nonEmptyJavaFile = new File(dir, "${className}.java")
    nonEmptyJavaFile << """
package ${packageName};

import com.episode6.hackit.chop.Chop;

/**
 * A sample class for testing
 */
public class ${className} {

  public void sendMessage() {
    Chop.i("hello");
  }

}
"""
    return nonEmptyJavaFile
  }
}
