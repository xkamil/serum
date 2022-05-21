package pl.net.testit.serum.api.response;

import io.restassured.response.Response;
import java.util.Optional;

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
    if (okResponseStatusCode != null && response.statusCode() != okResponseStatusCode) {
      throw new ApiResponseException(
          "Expected response status code " + okResponseStatusCode + " but was " + response.statusCode());
    } else if (response.getStatusCode() < 200 || response.getStatusCode() > 299) {
      throw new ApiResponseException(
          "Expected response status code between 200 and 299 but was " + response.statusCode());
    }
    return response.getBody().as(okResponseClass);
  }

  @SuppressWarnings("unchecked")
  public T2 assertError(int expectedStatusCode) {
    if (response.getStatusCode() != expectedStatusCode) {
      throw new ApiResponseException(
          "Expected response status code " + expectedStatusCode + " but was " + response.statusCode());
    }

    if (errorResponseClass.equals(String.class)) {
      return (T2) response.getBody().asPrettyString();
    }
    return response.getBody().as(errorResponseClass);
  }

  @SuppressWarnings("unchecked")
  public T2 assertError() {
    if (response.getStatusCode() < 400) {
      throw new ApiResponseException(
          "Expected error response status code but was " + response.statusCode());
    }

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
      throw new ApiResponseException("Expected header " + headerName + " not present in response");
    }
    return value;
  }

}
