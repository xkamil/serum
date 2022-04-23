package com.example.qa.kafka;

import java.util.Properties;

public class SerumKafkaClientBuilder {

  private final Properties consumerProperties;
  private final Properties adminProperties;
  private final Properties producerProperties;

  public SerumKafkaClientBuilder(String bootstrapServers) {
    this.consumerProperties = getDefaultConsumerProperties(bootstrapServers);
    this.adminProperties = getDefaultAdminProperties(bootstrapServers);
    this.producerProperties = getDefaultProducerProperties(bootstrapServers);
  }

  public SerumKafkaClientBuilder withConsumerProperty(String key, String value) {
    this.consumerProperties.setProperty(key, value);
    return this;
  }

  public SerumKafkaClientBuilder withAdminProperty(String key, String value) {
    this.adminProperties.setProperty(key, value);
    return this;
  }

  public SerumKafkaClientBuilder withProducerProperty(String key, String value) {
    this.producerProperties.setProperty(key, value);
    return this;
  }

  public SerumKafkaClient build() {
    return new SerumKafkaClient(consumerProperties, adminProperties, producerProperties);
  }

  private static Properties getDefaultConsumerProperties(String bootstrapServers) {
    var props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("auto.offset.reset", "earliest");
    return props;
  }

  private static Properties getDefaultAdminProperties(String bootstrapServers) {
    var props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("request.timeout.ms", "50000");
    props.put("default.api.timeout.ms", "50000");
    return props;
  }

  private static Properties getDefaultProducerProperties(String bootstrapServers) {
    var props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("acks", "all");
    props.put("retries", 1);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    return props;
  }
}
