package com.episode6.hackit.gdmc.throwable

import org.gradle.api.GradleException

/**
 * Exception thrown when gdmc fails to parse a json file
 */
class GdmcParseException extends GradleException {
  GdmcParseException(String message, Throwable cause) {
    super(message, cause)
  }
}
