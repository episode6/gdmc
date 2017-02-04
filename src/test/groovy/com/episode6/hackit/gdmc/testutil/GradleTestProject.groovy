package com.episode6.hackit.gdmc.testutil

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

/**
 * Trait for elements common to a root gradle project and a subproject
 */
trait GradleTestProject {

  String name
  File gradleBuildFile
  File settingsGradleFile

  abstract File getRoot()
  abstract void beforeTask()

  void initGradleTestProject() {
    name = root.name
    gradleBuildFile = root.newFile("build.gradle")
    settingsGradleFile = root.newFile("settings.gradle")
  }

  BuildResult build(String... argument) {
    beforeTask()
    return GradleRunner.create()
        .withProjectDir(root)
        .withPluginClasspath()
        .withArguments(argument)
        .build()
  }

  BuildResult buildAndFail(String... arguments) {
    beforeTask()
    return GradleRunner.create()
        .withProjectDir(root)
        .withPluginClasspath()
        .withArguments(arguments)
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