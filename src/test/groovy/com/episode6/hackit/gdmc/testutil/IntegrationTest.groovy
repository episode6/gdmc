package com.episode6.hackit.gdmc.testutil

import org.junit.rules.TemporaryFolder

/**
 * Integration test rule.
 */
class IntegrationTest extends TemporaryFolder implements GradleTestProject {
  File gdmcJsonFile
  List<SubProject> subProjects

  @Override
  protected void before() throws Throwable {
    super.before()
    initGradleTestProject()
    gdmcJsonFile = root.newFile("gdmc.json")
    subProjects = new LinkedList<>()
  }

  SubProject subProject(String name) {
    SubProject subProject = new SubProject(root.newFolder(name))
    subProjects.add(subProject)
    return subProject
  }

  @Override
  void beforeTask() {
    if (subProjects) {
      settingsGradleFile.text = """
include ':${subProjects.collect {it.name}.join("', ':")}'
"""
    } else {
      settingsGradleFile.text = """
rootProject.name = '${name}'
"""
    }
  }

  File singleGdmcOverrideFile(String filename = "gdmcOverrides.json") {
    newFile("gradle.properties") << """
gdmc.overrideFiles=${filename}
"""
    return newFile(filename)
  }

  static class SubProject implements GradleTestProject {
    File root

    SubProject(File root) {
      this.root = root
      initGradleTestProject()
    }

    @Override
    void beforeTask() {

    }
  }
}
