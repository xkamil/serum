package com.example.serum.commons.json;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class JsonParserTest {

  @Test
  void testFromJSON() {
    // given valid json string
    var jsonString = "{ \"firstName\": \"Roman\",\"lastName\": \"Nowak\",  \"age\": 22,  \"cash\": 1.234, \"a\": \"b\" } ";

    // when it's deserialized to class
    var deserialized = JsonParser.fromJSON(jsonString, Person.class);

    // then output object should be valid
    assertAll(
        () -> assertThat(deserialized.firstName).isEqualTo("Roman"),
        () -> assertThat(deserialized.age).isEqualTo(22),
        () -> assertThat(deserialized.cash).isEqualToIgnoringScale(BigDecimal.valueOf(1.234)),
        () -> assertThat(deserialized.getLastName()).isEqualTo("Nowak")
    );

  }

  @Test
  void testToMultilineJSON() {
    // given some object
    var obj = new Person("Jan", 33, BigDecimal.valueOf(44.55));

    // when it's serialized to json string
    var serialized = JsonParser.toMultilineJSON(obj).replaceAll("\\s+", "");

    // then output object should be valid
    var expectedOutput = "{\"firstName\":\"Jan\",\"age\":33,\"cash\":44.55}";
    assertThat(serialized).isEqualTo(expectedOutput);
  }

  @Test
  void testToInlineJSON() {
    // given some object
    var obj = new Person("Jan", 33, BigDecimal.valueOf(44.55));

    // when it's serialized to json string
    var serialized = JsonParser.toInlineJSON(obj).replaceAll("\\s+", "");

    // then output object should be valid
    var expectedOutput = "{\"firstName\":\"Jan\",\"age\":33,\"cash\":44.55}";
    assertThat(serialized).isEqualTo(expectedOutput);
  }


  public static class Person {

    public String firstName;
    public Integer age;
    public BigDecimal cash;
    private String lastName;

    public Person() {
    }

    public Person(String firstName, Integer age, BigDecimal cash) {
      this.firstName = firstName;
      this.age = age;
      this.cash = cash;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }
  }

}
