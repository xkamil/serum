package com.example.serum.commons.json;

public class JsonParseException extends RuntimeException {

  public JsonParseException(String message) {
    super(message);
  }

  public JsonParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
