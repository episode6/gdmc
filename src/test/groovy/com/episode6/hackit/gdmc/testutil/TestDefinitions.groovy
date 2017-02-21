package com.episode6.hackit.gdmc.testutil

/**
 * Definitions for test files.
 */
class TestDefinitions {
  static final TASK_RESOLVE = "gdmcResolve"
  static final TASK_IMPORT = "gdmcImport"
  static final TASK_IMPORT_TRANS = "gdmcImportTransitive"
  static final TASK_UPGRADE = "gdmcUpgrade"
  static final TASK_UPGRADE_ALL = "gdmcUpgradeAll"
  static final TASK_IMPORT_SELF = "gdmcImportSelf"

  static final GDMC_PLUGIN = "id 'com.episode6.hackit.gdmc'"
  static final GDMC_SPRINGS_COMPAT_PLUGIN = """
id 'com.episode6.hackit.gdmc-spring-compat'
id 'io.spring.dependency-management' version '1.0.0.RELEASE'
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

  static final String buildFilePrefix(String plugins, Map opts = [:]) {
    """
plugins {
  id 'groovy'
${plugins}
}

gdmcLogger {
 enable()
}

group = 'com.example.testproject'
version = '${opts.version ?: '0.0.1-SNAPSHOT'}'

repositories {
  jcenter()
  maven {
    url "https://oss.sonatype.org/content/repositories/snapshots/"
  }
}

"""
  }

  static void setupMultiProject(IntegrationTest test, String plugin, Map versions = [:]) {
    test.gradleBuildFile << """
allprojects {
  group = "com.example"
  version = "${versions.projectVersion ?: '0.0.1-SNAPSHOT'}"
  
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

gdmcLogger {
 enable()
}

dependencies {
   compile 'org.mockito:mockito-core${versions.mockitoVersion ?: ''}'
   compile 'com.episode6.hackit.chop:chop-core${versions.chopVersion ?: ''}'
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

gdmcLogger {
 enable()
}

dependencies {
   compile project(':javalib')
   compile 'com.episode6.hackit.chop:chop-core${versions.chopVersion ?: ''}'
   testCompile('org.spockframework:spock-core${versions.spockVersion ?: ''}') {
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
