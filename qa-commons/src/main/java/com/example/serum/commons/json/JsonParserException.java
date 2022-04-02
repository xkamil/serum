package com.example.serum.commons.json;

public class JsonParserException extends RuntimeException {

  public JsonParserException(String message) {
    super(message);
  }

  public JsonParserException(String message, Throwable cause) {
    super(message, cause);
  }
}
