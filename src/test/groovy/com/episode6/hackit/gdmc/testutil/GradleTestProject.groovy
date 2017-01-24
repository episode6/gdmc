package com.episode6.hackit.gdmc.testutil

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * Trait for elements common to a root gradle project and a subproject
 */
trait GradleTestProject {

  File gradleBuildFile
  File settingsGradleFile

  abstract File getRoot()

  void initGradleTestProject() {
    gradleBuildFile = root.newFile("build.gradle")
    settingsGradleFile = root.newFile("settings.gradle")
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