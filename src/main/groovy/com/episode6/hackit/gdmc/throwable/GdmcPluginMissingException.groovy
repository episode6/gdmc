package com.episode6.hackit.gdmc.throwable

/**
 * Thrown when gdmc() method is called on a project that doesn't apply the gdmc plugin
 */
class GdmcPluginMissingException extends RuntimeException {
  GdmcPluginMissingException() {
    super("gdmc() method called on a project that does not apply the gdmc plugin")
  }
}
