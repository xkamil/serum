package pl.net.testit.serum.api.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import pl.net.testit.serum.commons.object.ObjectUtils;

public abstract class RequestQueryParams {

  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface QueryParamName {

    String value();
  }

  public Map<String, String> asMap() {
    return Arrays.stream(this.getClass().getDeclaredFields())
        .filter(field -> !field.isSynthetic())
        .filter(field -> ObjectUtils.getFieldValue(field.getName(), this).isPresent())
        .collect(Collectors.toMap(
            this::getQueryParamName,
            field -> ObjectUtils.getFieldValue(field.getName(), this).orElse("")));
  }

  private String getQueryParamName(Field objectField) {
    var annotation = objectField.getAnnotation(QueryParamName.class);
    return annotation != null ? annotation.value() : objectField.getName();
  }

}
