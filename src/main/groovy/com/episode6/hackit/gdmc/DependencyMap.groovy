package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.DependencyMap.DependencyFilter
import com.episode6.hackit.gdmc.json.GdmcDependency

/**
 * interface for dependency mapper
 */
interface DependencyMap {
  interface DependencyFilter {
    boolean shouldApply(String key, GdmcDependency dependency)
  }

  String sanitizeKey(Object obj)
  List<GdmcDependency> lookup(Object key)
  void applyFile(File file)
  void applyFile(File file, DependencyFilter filter)
}
