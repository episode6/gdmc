package com.episode6.hackit.gdmc.util

import com.episode6.hackit.chop.Chop.ChoppingToolsAdapter
import com.episode6.hackit.chop.Chop.Tree
import com.episode6.hackit.chop.groovy.GroovyDebugTagger
import com.episode6.hackit.chop.tree.StdOutDebugTree

/**
 * Helper class to to turn on logging
 */
class GdmcLogger {
  static final ChoppingToolsAdapter Chop = com.episode6.hackit.chop.Chop.withTagger(new GroovyDebugTagger())

  private static final Tree STD_OUT_TREE = new StdOutDebugTree()

  void enable() {
    com.episode6.hackit.chop.Chop.plantTree(STD_OUT_TREE)
  }
}
