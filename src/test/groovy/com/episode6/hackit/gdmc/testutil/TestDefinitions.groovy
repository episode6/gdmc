package com.episode6.hackit.gdmc.testutil

/**
 * definitions for test files
 */
class TestDefinitions {
  static final GDMC_PLUGIN = "id 'com.episode6.hackit.gdmc'"
  static final GDMC_SPRINGS_COMPAT_PLUGIN = """
id 'com.episode6.hackit.gdmc-spring-compat'
id 'io.spring.dependency-management' version '1.0.0.RC2'
"""

  static final String CHOP_IMPORT = """
import com.episode6.hackit.chop.Chop;

"""
  static final String SPOCK_IMPORT = """
import spock.lang.Specification;

"""
  static final String MOCKITO_IMPORT = """
import org.mockito.Mockito;

"""

  static final String buildFilePrefix(String plugins) {
    """
plugins {
  id 'groovy'
${plugins}
}

group = 'com.example.testproject'
version = '0.0.1-SNAPSHOT'

repositories {
  jcenter()
  maven {
    url "https://oss.sonatype.org/content/repositories/snapshots/"
  }
}

"""
  }

  static void setupMultiProject(IntegrationTest test, String plugin) {
    test.gradleBuildFile << """
allprojects {
  group = "com.example"
  version = "0.0.1-SNAPSHOT"
  
  repositories {
    jcenter()
    maven {
      url "https://oss.sonatype.org/content/repositories/snapshots/"
    }
  }
}
"""
    test.subProject("javalib").with {
      gradleBuildFile << """
plugins {
  id 'java'
${plugin}
}

dependencies {
   compile 'org.mockito:mockito-core'
   compile 'com.episode6.hackit.chop:chop-core'
}
"""
      createJavaFile(
          packageName: "com.episode6.javalib",
          imports: CHOP_IMPORT)
      createJavaFile(
          packageName: "com.episode6.javalib",
          className: "SampleClass2",
          imports: MOCKITO_IMPORT)
    }
    test.subProject("groovylib").with {
      gradleBuildFile << """
plugins {
  id 'groovy'
${plugin}
}
dependencies {
   compile project(':javalib')
   compile 'com.episode6.hackit.chop:chop-core'
   testCompile(group: 'org.spockframework', name: 'spock-core') {
    exclude module: 'groovy-all'
  }
}
"""
      createJavaFile(
          packageName: "com.episode6.groovylib",
          imports: CHOP_IMPORT)
      createJavaFile(
          packageName: "com.episode6.groovylib",
          className: "SampleClassTest",
          dir: "test",
          imports: SPOCK_IMPORT)
    }
  }
}
