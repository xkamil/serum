package com.example.serum.commons.json;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.example.serum.commons.object.ObjectUtils;
import org.junit.jupiter.api.Test;

public class ObjectUtilsTest {

  @Test
  void getFieldValue() {
    // given some object
    class ExampleObject {

      public final String publicField = "value1";
      public String publicNullField = null;
      private String privateField = "value2";
      protected String protectedField = "value3";
      private static final String staticPrivateField = "value4";
      public static final String staticPublicField = "value5";

    }
    var obj = new ExampleObject();

    // when getFieldValue is executed for each field
    var publicFieldValue = ObjectUtils.getFieldValue("publicField", obj).orElseThrow();
    var privateField = ObjectUtils.getFieldValue("privateField", obj).orElseThrow();
    var protectedField = ObjectUtils.getFieldValue("protectedField", obj).orElseThrow();
    var staticPrivateField = ObjectUtils.getFieldValue("staticPrivateField", obj).orElseThrow();
    var staticPublicField = ObjectUtils.getFieldValue("staticPublicField", obj).orElseThrow();
    var publicNullField = ObjectUtils.getFieldValue("publicNullField", obj);

    // then output object should be valid
    assertAll(
        () -> assertThat(publicFieldValue).isEqualTo("value1"),
        () -> assertThat(privateField).isEqualTo("value2"),
        () -> assertThat(protectedField).isEqualTo("value3"),
        () -> assertThat(staticPrivateField).isEqualTo("value4"),
        () -> assertThat(staticPublicField).isEqualTo("value5"),
        () -> assertThat(publicNullField.isEmpty()).isTrue()
    );

  }


}
