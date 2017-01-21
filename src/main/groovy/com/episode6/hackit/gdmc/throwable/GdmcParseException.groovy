package com.episode6.hackit.gdmc.throwable

/**
 * Exception thrown when gdmc fails to parse a json file
 */
class GdmcParseException extends RuntimeException {
  GdmcParseException(String message, Throwable cause) {
    super(message, cause)
  }
}
