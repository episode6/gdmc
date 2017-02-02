package com.episode6.hackit.gdmc

import com.episode6.hackit.chop.Chop
import com.episode6.hackit.chop.Chop.Tagger
import com.episode6.hackit.chop.Chop.Tree
import com.episode6.hackit.chop.groovy.GroovyDebugTagger
import com.episode6.hackit.chop.tree.StdOutDebugTree

/**
 * helper class to to turn on logging
 */
class GdmcLogger {
  private static final Tree STD_OUT_TREE = new StdOutDebugTree()
  private static final Tagger GROOVY_TAGGER = new GroovyDebugTagger()

  void enable() {
    Chop.withTagger(GROOVY_TAGGER).byDefault()
    Chop.plantTree(STD_OUT_TREE)
  }
}
