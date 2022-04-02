package com.example.serum.api.filter;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Optional;


public class RequestHeaderFilter implements Filter {

  private final String headerName;
  private String headerValue;

  public RequestHeaderFilter(String headerName) {
    this.headerName = headerName;
  }

  public void setValue(String token) {
    this.headerValue = token;
  }

  public void removeValue() {
    this.headerValue = null;
  }

  @Override
  public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
    var reqHeaders = req.getHeaders();
    req.removeHeaders();

    reqHeaders.asList().stream()
        .filter(h -> !h.getName().equalsIgnoreCase(headerName))
        .forEach(req::header);

    Optional.ofNullable(headerValue).ifPresent(token -> req.header(headerName, token));

    return ctx.next(req, res);
  }
}
