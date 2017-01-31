package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency

/**
 * interface for dependency mapper
 */
interface DependencyMap {
  String sanitizeKey(Object obj)
  List<GdmcDependency> lookup(Object key)
  void applyFile(File file)
}
