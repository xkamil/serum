package com.example.serum.api.filter;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.function.Supplier;

public class RequestHeaderFilter implements Filter {

  private final String headerName;
  private String staticValue;
  private Supplier<String> valueGenerator;
  private String valueForNextRequest;

  public RequestHeaderFilter(String headerName) {
    this.headerName = headerName;
    this.valueForNextRequest = null;
  }

  /**
   * Set static header value for all requests
   *
   * @param value - header value
   */
  public RequestHeaderFilter setValue(String value) {
    this.staticValue = value;
    this.valueGenerator = null;
    return this;
  }

  /**
   * Set header value generator. Generator will generate new value for each request
   *
   * @param valueGenerator - header value generator
   */
  public RequestHeaderFilter setValue(Supplier<String> valueGenerator) {
    this.valueGenerator = valueGenerator;
    this.staticValue = null;
    return this;
  }

  /**
   * Set header value that will be sent in next request and then it will be removed, so for other requests static header
   * value or header value generator will provide value.
   *
   * @param newValue - header value for next request
   */
  public RequestHeaderFilter setValueForNextRequest(String newValue) {
    this.valueForNextRequest = newValue;
    return this;
  }

  @Override
  public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
    var reqHeaders = req.getHeaders();
    req.removeHeaders();

    reqHeaders.asList().stream()
        .filter(h -> !h.getName().equalsIgnoreCase(headerName))
        .forEach(req::header);

    if (valueForNextRequest != null) {
      req.header(headerName, valueForNextRequest);
      valueForNextRequest = null;
    } else if (staticValue != null) {
      req.header(headerName, staticValue);
    } else if (valueGenerator != null) {
      req.header(headerName, valueGenerator.get());
    }

    return ctx.next(req, res);
  }
}
