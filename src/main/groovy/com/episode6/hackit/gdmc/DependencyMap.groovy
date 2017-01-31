package com.episode6.hackit.gdmc

import com.episode6.hackit.gdmc.json.GdmcDependency

/**
 * interface for dependency mapper
 */
interface DependencyMap {
  String sanitizeKey(Object obj)
  List<GdmcDependency> lookup(Object key)
  void applyDependencies(Set<GdmcDependency> newDependencies)
  void applyUpgradedDependencies(Set<GdmcDependency> newDependencies)
  void applyMissingDependencies(Set<GdmcDependency> newDependencies)
}
