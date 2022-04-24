package pl.net.testit.serum.commons.json;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

public class JsonEntityTest {

  @Test
  void testToJsonString() {
    // given object extends JsonEntity
    var obj = new Person("John", 33);

    // when toJsonString is executed
    var result = obj.toJsonString();
    var expectedResult = "{\n"
        + "  \"firstName\" : \"John\",\n"
        + "  \"sex\" : \"male\"\n"
        + "}";

    // then result should be multiline json string
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void testToInlineJsonString() {
    // given object extends JsonEntity
    var obj = new Person("John", 33);

    // when toInlineJsonString is executed
    var result = obj.toInlineJsonString();
    var expectedResult = "{\"firstName\":\"John\",\"sex\":\"male\"}";

    // then result should be inline json string
    assertThat(result).isEqualTo(expectedResult);
  }

  public static class Person extends JsonEntity {

    public String firstName;
    private Integer age;

    public Person(String firstName, Integer age) {
      this.firstName = firstName;
      this.age = age;
    }

    public String getSex() {
      return "male";
    }
  }

}
