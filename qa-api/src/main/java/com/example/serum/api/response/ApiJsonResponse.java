package com.example.serum.api.response;

import static com.google.common.truth.Truth.assertThat;

import io.restassured.response.Response;
import java.util.Optional;
import org.junit.Assert;

public class ApiJsonResponse<T1, T2> {

  private final Response response;
  private final Integer okResponseStatusCode;
  private final Class<T1> okResponseClass;
  private final Class<T2> errorResponseClass;

  public static <T1, T2> ApiJsonResponse<T1, T2> from(Response response, Class<T1> okResponseClass,
      int okResponseStatusCode, Class<T2> errorResponseClass) {
    return new ApiJsonResponse<T1, T2>(response, okResponseClass, okResponseStatusCode, errorResponseClass);
  }

  public static <T1, T2> ApiJsonResponse<T1, T2> from(Response response, Class<T1> okResponseClass,
      Class<T2> errorResponseClass) {
    return new ApiJsonResponse<T1, T2>(response, okResponseClass, null, errorResponseClass);
  }

  private ApiJsonResponse(Response response, Class<T1> okResponseClass, Integer okResponseStatusCode,
      Class<T2> errorResponseClass) {
    this.response = response;
    this.okResponseClass = okResponseClass;
    this.okResponseStatusCode = okResponseStatusCode;
    this.errorResponseClass = errorResponseClass;
  }

  public T1 assertOk() {
    if (okResponseStatusCode != null) {
      assertThat(response.statusCode()).isEqualTo(okResponseStatusCode);
    } else {
      assertThat(response.statusCode()).isAtLeast(200);
      assertThat(response.statusCode()).isAtMost(299);
    }
    return response.getBody().as(okResponseClass);
  }

  @SuppressWarnings("unchecked")
  public T2 assertError(int expectedStatusCode) {
    assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
    if (errorResponseClass.equals(String.class)) {
      return (T2) response.getBody().asPrettyString();
    }
    return response.getBody().as(errorResponseClass);
  }

  @SuppressWarnings("unchecked")
  public T2 assertError() {
    assertThat(response.statusCode()).isAtLeast(400);
    if (errorResponseClass.equals(String.class)) {
      return (T2) response.getBody().asPrettyString();
    }
    return response.getBody().as(errorResponseClass);
  }

  public Response getRawResponse() {
    return response;
  }

  public Optional<String> getHeader(String headerName) {
    return Optional.ofNullable(this.response.getHeader(headerName));
  }

  public String getHeaderOrFail(String headerName) {
    var value = this.response.getHeader(headerName);
    if (value == null) {
      Assert.fail(String.format("Header %s not present in response", headerName));
    }
    return value;
  }

}
