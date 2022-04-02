package com.example.serum.api.response;

import static com.google.common.truth.Truth.assertThat;

import io.restassured.response.Response;

public class ApiResponse<T1, T2> {

  private final Response response;
  private final Class<T1> okResponseClass;
  private final Class<T2> errorResponseClass;

  public ApiResponse(Response response, Class<T1> okResponseClass, Class<T2> errorResponseClass) {
    this.response = response;
    this.okResponseClass = okResponseClass;
    this.errorResponseClass = errorResponseClass;
  }

  public T1 ok(int expectedStatusCode) {
    assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
    return response.as(okResponseClass);
  }

  public T2 error(int expectedStatusCode) {
    assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
    return response.as(errorResponseClass);
  }

}
