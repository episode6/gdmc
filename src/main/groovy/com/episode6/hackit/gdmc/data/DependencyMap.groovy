package com.episode6.hackit.gdmc.data

/**
 * Interface for dependency mapper
 */
interface DependencyMap {
  interface DependencyFilter {
    boolean shouldApply(String key, GdmcDependency dependency)
  }

  boolean isSourceAlias(Object key)
  boolean isOverrideAlias(Object key)
  boolean isLocked(Object key)
  List<GdmcDependency> lookupFromSource(Object key)
  List<GdmcDependency> lookupWithOverrides(Object key)
  List<GdmcDependency> getValidDependencies()
  void applyFile(File file)
  void applyFile(File file, DependencyFilter filter)
  void applyOverrides(File file)
  void put(GdmcDependency dependency)
}
