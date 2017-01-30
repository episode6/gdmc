package com.episode6.hackit.gdmc

/**
 * interface for dependency mapper
 */
interface DependencyMap {
  /**
   * returns objectNotation for dependency or array of dependencies (for aliases)
   */
  Object lookup(Object key)
}
