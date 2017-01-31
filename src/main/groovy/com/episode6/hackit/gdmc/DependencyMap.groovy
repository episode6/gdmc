package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency

/**
 * interface for dependency mapper
 */
interface DependencyMap {
  List<GdmcDependency> lookup(Object key)
}
