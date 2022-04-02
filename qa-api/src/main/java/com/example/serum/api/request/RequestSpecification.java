package com.example.serum.api.request;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

public class RequestSpecification {

  public static RestAssuredConfig baseRequestSpec() {

    var requestConfig = RequestConfig.custom()
        .setConnectTimeout(20_000)
        .setConnectionRequestTimeout(20_000)
        .setSocketTimeout(20_000)
        .build();

    var httpClientConfig = HttpClientConfig.httpClientConfig()
        .httpClientFactory(() -> HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .build());

    var objectMapperConfig = ObjectMapperConfig.objectMapperConfig()
        .jackson2ObjectMapperFactory((aClass, s) -> com.example.serum.commons.json.JsonParser.getObjectMapper());

    return RestAssuredConfig.newConfig()
        .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation().allowAllHostnames())
        .httpClient(httpClientConfig)
        .objectMapperConfig(objectMapperConfig);
  }

}
