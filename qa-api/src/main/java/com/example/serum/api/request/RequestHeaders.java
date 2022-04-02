package com.example.serum.api.request;

import com.example.serum.commons.object.ObjectUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class RequestHeaders {

  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface HeaderName {

    String value();
  }

  public Map<String, String> asMap() {
    var headers = new HashMap<String, String>();

    for (Field declaredField : this.getClass().getDeclaredFields()) {
      var annotation = declaredField.getAnnotation(HeaderName.class);
      var headerName = annotation != null ? annotation.value() : declaredField.getName();
      ObjectUtils.getFieldValue(declaredField.getName(), this).ifPresent(value -> headers.put(headerName, value));
    }
    return headers;
  }

}
