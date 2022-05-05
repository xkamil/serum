package pl.net.testit.serum.api.filter;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Map;

public class RequestQueryParamsFilter implements Filter {

  private Map<String, String> queryParamsForNextRequest;

  public RequestQueryParamsFilter setQueryParamsForNextRequest(Map<String, String> queryParams) {
    this.queryParamsForNextRequest = queryParams;
    return this;
  }

  @Override
  public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
    if (queryParamsForNextRequest != null) {
      req.queryParams(queryParamsForNextRequest);
      queryParamsForNextRequest = null;
    }

    return ctx.next(req, res);
  }
}
