package com.episode6.hackit.gdmc.throwable

import org.gradle.api.GradleException

/**
 * Thrown when gdmc() method is called on a project that doesn't apply the gdmc plugin
 */
class GdmcPluginMissingException extends GradleException {
  GdmcPluginMissingException() {
    super("gdmc() method called on a project that does not apply the gdmc plugin")
  }
}
