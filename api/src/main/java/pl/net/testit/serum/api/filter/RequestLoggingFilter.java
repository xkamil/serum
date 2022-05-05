package pl.net.testit.serum.api.filter;

import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RequestLoggingFilter implements OrderedFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  @Override
  public Response filter(FilterableRequestSpecification req, FilterableResponseSpecification res, FilterContext ctx) {
    log.info("Request:\n\n{}", asCurl(req));
    return ctx.next(req, res);
  }

  private String asCurl(FilterableRequestSpecification request) {
    var curl = new StringBuilder();
    curl.append(String.format("curl --request %s '%s' \\\n", request.getMethod().toUpperCase(), request.getURI()));

    if (request.getBody() != null) {
      curl.append(String.format("--data '%s' \\\n", request.getBody().toString()));
    }

    if (request.getFormParams() != null && request.getFormParams().size() > 0) {
      var formParamsInline = request.getFormParams().entrySet().stream()
          .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
          .collect(Collectors.joining("&"));
      curl.append(String.format("--data '%s' \\\n", formParamsInline));
    }

    request.getHeaders().asList()
        .forEach(h -> curl.append(String.format("--header '%s: %s' \\\n", h.getName(), h.getValue())));

    request.getCookies().asList()
        .forEach(c -> curl.append(String.format("--cookie '%s=%s' \\\n", c.getName(), c.getValue())));

    curl.append("--insecure --silent --show-error --include \n ");

    if (request.getMultiPartParams() != null && request.getMultiPartParams().size() > 0) {
      curl.append("\nRequest data preview for multipart form params:");
      curl.append("\n--------------------------------------------\n");
      var formSB = new StringBuilder();
      request.getMultiPartParams()
          .forEach(k -> formSB.append(String.format("[%s] = %s\n", k.getControlName(), k.getContent())));
      curl.append(formSB).append("' \n");
    }

    return curl.toString();
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }
}
