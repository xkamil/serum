package com.example.serum.commons.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonParser {

  private static final Logger logger = LoggerFactory.getLogger(JsonParser.class);
  private static final ObjectMapper objectMapper;
  private static final DefaultPrettyPrinter multilineJsonPrinter;

  private static final DeserializationProblemHandler unreconizedPropertyHandler = new DeserializationProblemHandler() {
    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt, com.fasterxml.jackson.core.JsonParser p,
        JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
      var wasHandled = ctxt.getAttribute(beanOrClass.getClass()) != null && ctxt.getAttribute(beanOrClass.getClass())
          .equals(propertyName);

      if (!wasHandled) {
        logger.warn("Missing property '{}' in target deserialization class {}", propertyName, beanOrClass.getClass());
        ctxt.setAttribute(beanOrClass.getClass(), propertyName);
        try {
          ctxt.getParser().skipChildren();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      return true;
    }
  };

  static {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    // numbers will be mapped and printed with all zeros
    objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

    // dates will be serialized to ISO-8601 formatted string
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // nulls or Nones will be skipped in serialized string
    objectMapper.setSerializationInclusion(Include.NON_NULL);

    // deserialization will not fail if new property (not defined in class) occurs in json
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.addHandler(unreconizedPropertyHandler);

    // deserialization will not fail if model property is present in json and missing in target model
    objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);

    // no need to fail serialization if class doesn't have public fields/getters
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    multilineJsonPrinter = new DefaultPrettyPrinter();
    DefaultIndenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
    multilineJsonPrinter.indentObjectsWith(indenter);
    multilineJsonPrinter.indentArraysWith(indenter);
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public static String toInlineJSON(Object any) {
    try {
      return objectMapper.writeValueAsString(any);
    } catch (JsonProcessingException e) {
      throw new JsonParseException("Cannot create Json from object.", e);
    }
  }

  public static String toMultilineJSON(Object any) {
    try {
      return objectMapper.writer(multilineJsonPrinter).writeValueAsString(any);
    } catch (JsonProcessingException e) {
      throw new JsonParseException("Cannot create Json from object.", e);
    }
  }

  public static <T> T fromJSON(String json, Class<T> cls) {
    try {
      return objectMapper.readValue(json, cls);
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot create object of class: " + cls.getName() + " from Json", e);
    }
  }

}
