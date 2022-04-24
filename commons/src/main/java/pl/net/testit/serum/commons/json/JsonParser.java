package pl.net.testit.serum.commons.json;

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
  private static final ObjectMapper strictObjectMapper;
  private static final DefaultPrettyPrinter multilineJsonPrinter;

  private static final DeserializationProblemHandler unreconizedPropertyHandler = new DeserializationProblemHandler() {
    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt, com.fasterxml.jackson.core.JsonParser p,
        JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
      var wasHandled = ctxt.getAttribute(beanOrClass.getClass()) != null && ctxt.getAttribute(beanOrClass.getClass())
          .equals(propertyName);

      if (!wasHandled) {
        logger.warn("Missing property '{}' in target deserialization {}", propertyName, beanOrClass.getClass());
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

    // deserialize floats to BigDecimal
    objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    // datesserialized to ISO-8601
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    // fields with null values not serialized
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    // deserialization will not fail if model property is present in json and missing in target model
    objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
    // don't fail serialization if class doesn't have public fields/getters
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    strictObjectMapper = objectMapper.copy();
    // fail deserialization if property not defined in class occurs in json
    strictObjectMapper.addHandler(getUnreconizedPropertyHandler(true));

    // dont't fail deserialization if property not defined in class occurs in json
    objectMapper.addHandler(getUnreconizedPropertyHandler(false));

    multilineJsonPrinter = new DefaultPrettyPrinter();
    var indenter = new DefaultIndenter("  ", "\n");
    multilineJsonPrinter.indentObjectsWith(indenter);
    multilineJsonPrinter.indentArraysWith(indenter);
  }

  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public static ObjectMapper getStrictObjectMapper() {
    return objectMapper;
  }

  /**
   * Serialize object to inline json string
   *
   * @param obj - object to be serialized
   * @return inline json string
   * @throws JsonParseException if serialization fails
   */
  public static String toInlineJSON(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new JsonParseException("Cannot create Json from object.", e);
    }
  }

  /**
   * Serialize object to multiline formatted json string
   *
   * @param obj - object to be serialized
   * @return formatted json string
   * @throws JsonParseException if serialization fails
   */
  public static String toMultilineJSON(Object obj) {
    try {
      return objectMapper.writer(multilineJsonPrinter).writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new JsonParseException("Cannot create Json from object.", e);
    }
  }

  /**
   * Deserialize json string to object.
   *
   * @param json - json string
   * @param cls - target deserialization class
   * @param <T> - any type
   * @return json deserialized to target deserialization class
   * @throws JsonParseException if deserializaion fails
   */
  @SuppressWarnings("unchecked")
  public static <T> T fromJSON(String json, Class<T> cls) {
    try {
      if (cls.equals(String.class)) {
        return (T) json;
      }
      return objectMapper.readValue(json, cls);
    } catch (IOException e) {
      throw new JsonParseException("Cannot create object of class: " + cls.getName() + " from Json", e);
    }
  }

  /**
   * Deserialize json string to object. Requires target class to have fields mapping for all properties present in
   * deserialized json
   *
   * @param json - json string
   * @param cls - target deserialization class
   * @param <T> - any type
   * @return json deserialized to target deserialization class
   * @throws JsonParseException if deserializaion fails and if json string contains properties that target class has no
   * mapping for
   */
  @SuppressWarnings("unchecked")
  public static <T> T strictFromJSON(String json, Class<T> cls) {
    try {
      if (cls.equals(String.class)) {
        return (T) json;
      }
      return strictObjectMapper.readValue(json, cls);
    } catch (IOException e) {
      throw new JsonParseException("Cannot create object of class: " + cls.getName() + " from Json", e);
    }
  }

  private static DeserializationProblemHandler getUnreconizedPropertyHandler(boolean failOnUnreconizedProperty) {
    return new DeserializationProblemHandler() {
      @Override
      public boolean handleUnknownProperty(DeserializationContext ctxt, com.fasterxml.jackson.core.JsonParser p,
          JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
        var wasHandled = ctxt.getAttribute(beanOrClass.getClass()) != null && ctxt.getAttribute(beanOrClass.getClass())
            .equals(propertyName);

        if (!wasHandled) {
          logger.warn("Missing property '{}' in target deserialization {}", propertyName, beanOrClass.getClass());
          ctxt.setAttribute(beanOrClass.getClass(), propertyName);
          try {
            ctxt.getParser().skipChildren();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        return !failOnUnreconizedProperty;
      }
    };
  }
}
