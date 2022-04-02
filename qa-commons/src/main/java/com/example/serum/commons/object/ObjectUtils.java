package com.example.serum.commons.object;

import java.util.Optional;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ObjectUtils {

  public static Optional<String> getFieldValue(String fieldName, Object sourceObject) {
    try {
      var value = FieldUtils.readField(sourceObject, fieldName, true);
      return Optional.ofNullable(value).map(Object::toString);
    } catch (IllegalAccessException e) {
      throw new ObjectUtilsException(String.format("Unable to read field %s from %s", fieldName, sourceObject), e);
    }
  }
}
