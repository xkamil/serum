package pl.net.testit.serum.api.filter;

import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyData;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseLoggingFilter implements OrderedFilter {

  private static final Logger log = LoggerFactory.getLogger(ResponseLoggingFilter.class);

  @Override
  public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
    var response = ctx.next(req, res);
    var responseTime = response.getTimeIn(TimeUnit.MILLISECONDS);
    log.info("Response ({} ms) {}\n", responseTime, getResponseAsFormattedString(response));
    return response;
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }

  private String getResponseAsFormattedString(Response response) {
    var sb = new StringBuilder();
    sb.append(String.format("\n%s\n", response.getStatusLine()));
    response.getHeaders().asList().forEach(h -> sb.append(String.format("%s: %s\n", h.getName(), h.getValue())));
    Optional.ofNullable(response.body())
        .map(ResponseBodyData::asPrettyString)
        .filter(f -> !f.isEmpty())
        .ifPresentOrElse(sb::append, () -> sb.append("[no body]"));
    sb.append("\n");
    return sb.toString();
  }
}
