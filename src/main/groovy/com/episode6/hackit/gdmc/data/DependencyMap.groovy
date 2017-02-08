package com.episode6.hackit.gdmc.data

/**
 * Interface for dependency mapper
 */
interface DependencyMap {
  interface DependencyFilter {
    boolean shouldApply(String key, GdmcDependency dependency)
  }

  List<GdmcDependency> lookup(Object key)
  List<GdmcDependency> getValidDependencies()
  void applyFile(File file)
  void applyFile(File file, DependencyFilter filter)
}
