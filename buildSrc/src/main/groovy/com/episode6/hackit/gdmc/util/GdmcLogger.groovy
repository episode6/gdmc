package com.episode6.hackit.gdmc.util

import com.episode6.hackit.chop.Chop
import com.episode6.hackit.chop.Chop.ChoppingToolsAdapter
import com.episode6.hackit.chop.Chop.Tree
import com.episode6.hackit.chop.groovy.GroovyDebugTagger
import com.episode6.hackit.chop.slf4j.Slf4jTree

/**
 * Helper class to to turn on logging
 */
class GdmcLogger {
  static final ChoppingToolsAdapter GChop = Chop.withTagger(new GroovyDebugTagger())

  private static final Tree TREE = Slf4jTree.create("gdmc")

  void enable() {
    Chop.plantTree(TREE)
  }
}
