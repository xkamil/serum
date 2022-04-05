package com.example.serum.commons.json;

import com.example.serum.commons.json.JsonParser;

public abstract class JsonEntity {

  public String toJsonString() {
    return JsonParser.toMultilineJSON(this);
  }

  public String toInlineJsonString() {
    return JsonParser.toInlineJSON(this);
  }
}
