package com.example.qa.kafka;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaClient {

  private static final ExecutorService executorService = Executors
      .newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());
  private final Properties properties;

  public static Builder builder(String bootstrapServers) {
    return new Builder(bootstrapServers);
  }

  public static class Builder {

    private final Properties properties;

    public Builder(String bootstrapServers) {
      this.properties = getDefaultProperties(bootstrapServers);
    }

    public Builder withProperty(String key, String value) {
      this.properties.setProperty(key, value);
      return this;
    }

    public KafkaClient build() {
      return new KafkaClient(properties);
    }
  }

  private KafkaClient(Properties properties) {
    this.properties = properties;
  }

  public <T> KafkaSimpleConsumer<T> subscribe(Class<T> messageClass) {
    var config = Optional.ofNullable(messageClass.getAnnotation(KafkaEvent.class)).orElseThrow(
        () -> new RuntimeException("messageClass should be annotated by KafkaEvent")
    );
    return new KafkaSimpleConsumer<T>(executorService, config.topic(), config.filter(), properties, messageClass)
        .subscribe();
  }

  private static Properties getDefaultProperties(String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("group.id", "qa-auto-" + UUID.randomUUID());
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("auto.offset.reset", "earliest");
    return props;
  }

}
