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

  def "placeholder test"() {
    given:
    buildFolder.newFile("build.gradle") << """
plugins {
  id 'java'
  id 'com.episode6.hackit.gdmc'
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
}

dependencies {
   compile gdmc('chop')
}
"""
    createNonEmptyJavaFile("com.episode6.testproject")
    File gdmcFolder = buildFolder.newFolder("gdmc")
    new File(gdmcFolder, "gdmc.json") << """
{
  "chop": {
    "alias": "com.episode6.hackit.chop:chop-core"
  },
  "com.episode6.hackit.chop:chop-core": {
      "groupId": "com.episode6.hackit.chop",
      "artifactId": "chop-core",
      "version": "0.1.7.2"
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
//    result.output.contains("output from testMethod(): holla back")
  }

  File createNonEmptyJavaFile(String packageName, String className = "SampleClass", File rootDir = buildFolder.getRoot()) {
    File dir = rootDir
    "src.main.java.${packageName}".tokenize(".").each {
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
