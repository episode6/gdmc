package com.episode6.hackit.gdmc.testutil

import org.junit.rules.TemporaryFolder

/**
 * Integration test rule
 */
class IntegrationTest extends TemporaryFolder implements GradleTestProject {
  File gdmcJsonFile

  @Override
  protected void before() throws Throwable {
    super.before()
    initGradleTestProject()
    gdmcJsonFile = root.newFile("gdmc.json")
  }
}
