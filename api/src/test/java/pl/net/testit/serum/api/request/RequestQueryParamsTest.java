package pl.net.testit.serum.api.request;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class RequestQueryParamsTest {

  @Test
  void testRequestQueryParamsAsMap() {
    // given example headers entity
    var queryParams = new ExampleQueryParams("value1", 22, 33, "value?=+4", null);

    // when asMap is executed
    var asMap = queryParams.asMap();

    // then it should return valid result
    var expectedResult = Map.of(
        "param1Name", "value1",
        "param2", "22",
        "param3", "33",
        "param?=&4", "value?=+4"
    );

    assertThat(asMap).containsExactlyEntriesIn(expectedResult);
  }

  public static class ExampleQueryParams extends RequestQueryParams {

    @QueryParamName("param1Name")
    public final String param1;

    @QueryParamName("param2")
    public final Integer param2;

    public final Integer param3;

    @QueryParamName("param?=&4")
    public final String param4;

    public final String param5;

    public ExampleQueryParams(String param1, Integer param2, Integer param3, String param4, String param5) {
      this.param1 = param1;
      this.param2 = param2;
      this.param3 = param3;
      this.param4 = param4;
      this.param5 = param5;
    }
  }
}
