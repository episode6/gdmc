package com.episode6.hackit.gdmc.testutil

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder

/**
 * Integration test rule
 */
class IntegrationTest extends TemporaryFolder {
  File gradleBuildFile
  File gdmcJsonFile

  @Override
  protected void before() throws Throwable {
    super.before()
    gradleBuildFile = root.newFile("build.gradle")
    gdmcJsonFile = root.newFile("gdmc.json")
  }

  BuildResult runTask(String taskName) {
    return GradleRunner.create()
        .withProjectDir(root)
        .withPluginClasspath()
        .withArguments(taskName)
        .build()
  }

  BuildResult runTaskAndFail(String taskName) {
    return GradleRunner.create()
        .withProjectDir(root)
        .withPluginClasspath()
        .withArguments(taskName)
        .buildAndFail()
  }

  File createJavaFile(Map params) {
    String packageName = params.packageName
    String className = params.className == null ? "SampleClass" : params.className
    String dir = params.dir == null ? "main" : params.dir
    String imports = params.imports == null ? "" : params.imports

    File javaFile = root.newFolderFromPackage("src.${dir}.java.${packageName}").newFile("${className}.java")
    javaFile << """
package ${packageName};

${imports}

/**
 * A sample class for testing
 */
public class ${className} {

  public void sampleMethod() {
  }

}
"""
    return javaFile
  }
}
