package pl.net.testit.serum.commons.json;

public abstract class JsonEntity {

  public String toJsonString() {
    return JsonParser.toMultilineJSON(this);
  }

  public String toInlineJsonString() {
    return JsonParser.toInlineJSON(this);
  }
}
