package com.example.serum.api.response;

import static com.google.common.truth.Truth.assertThat;

import io.restassured.response.Response;

public class ApiResponse<T1, T2> {

  private final Response response;
  private final Integer okResponseStatusCode;
  private final Class<T1> okResponseClass;
  private final Class<T2> errorResponseClass;

  public ApiResponse(Response response, Class<T1> okResponseClass, int okResponseStatusCode,
      Class<T2> errorResponseClass) {
    this.response = response;
    this.okResponseClass = okResponseClass;
    this.okResponseStatusCode = okResponseStatusCode;
    this.errorResponseClass = errorResponseClass;
  }

  public ApiResponse(Response response, Class<T1> okResponseClass, Class<T2> errorResponseClass) {
    this.response = response;
    this.okResponseClass = okResponseClass;
    this.okResponseStatusCode = null;
    this.errorResponseClass = errorResponseClass;
  }

  public T1 ok() {
    if (okResponseStatusCode != null) {
      assertThat(response.statusCode()).isEqualTo(okResponseStatusCode);
    } else {
      assertThat(response.statusCode()).isAtLeast(200);
      assertThat(response.statusCode()).isAtMost(299);
    }
    return response.as(okResponseClass);
  }

  public T2 error(int expectedStatusCode) {
    assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
    return response.as(errorResponseClass);
  }

  public T2 error() {
    assertThat(response.statusCode()).isAtLeast(400);
    return response.as(errorResponseClass);
  }

}
