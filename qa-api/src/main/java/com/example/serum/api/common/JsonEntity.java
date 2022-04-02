package com.example.serum.api.common;

import com.example.serum.commons.json.JsonParser;

public abstract class JsonEntity {

  @Override
  public String toString() {
    return JsonParser.toMultilineJSON(this);
  }

  public String toInlineString() {
    return JsonParser.toInlineJSON(this);
  }
}
