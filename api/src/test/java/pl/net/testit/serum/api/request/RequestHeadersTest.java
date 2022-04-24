package pl.net.testit.serum.api.request;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

public class RequestHeadersTest {

  @Test
  void testRequestHeadersAsMap() {
    // given example headers entity
    var headers = new ExampleHeaders("ASC", 10, 2);

    // when asMap is executed
    var asMap = headers.asMap();

    // then it should return valid result
    var expectedResult = Map.of(
        "filterOrder", "ASC",
        "limit", "10",
        "page", "2"
    );

    assertThat(asMap).containsExactlyEntriesIn(expectedResult);
  }


  public static class ExampleHeaders extends RequestHeaders {

    @HeaderName("filterOrder")
    public final String order;

    @HeaderName("limit")
    public final Integer limit;

    public final Integer page;

    public ExampleHeaders(String order, Integer limit, Integer page) {
      this.order = order;
      this.limit = limit;
      this.page = page;
    }
  }
}
