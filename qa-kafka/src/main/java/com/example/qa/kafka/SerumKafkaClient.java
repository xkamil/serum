package com.example.qa.kafka;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SerumKafkaClient {

  private static final Logger log = LoggerFactory.getLogger(SerumKafkaClient.class);
  private static final ExecutorService executorService = Executors
      .newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

  private final Map<String, SerumKafkaConsumer<?>> consumers = new HashMap<>();
  private KafkaProducer<String, String> producer;
  private final String consumerGroupPrefix;
  private final Properties consumerProperties;
  private final Properties adminProperties;
  private final Properties producerProperties;

  private SerumKafkaClient(String bootstrapServers, Properties consumerProperties, Properties adminProperties,
      Properties producerProperties) {
    this.consumerProperties = consumerProperties;
    this.adminProperties = adminProperties;
    this.producerProperties = producerProperties;
    this.consumerGroupPrefix = "test-";
  }

  public static Builder builder(String bootstrapServers) {
    return new Builder(bootstrapServers);
  }

  public <T> SerumKafkaConsumer<T> subscribe(Class<T> messageClass) {
    var config = Optional.ofNullable(messageClass.getAnnotation(KafkaEvent.class)).orElseThrow(
        () -> new RuntimeException("messageClass should be annotated by KafkaEvent")
    );
    var consumer = new SerumKafkaConsumer<T>(
        executorService,
        consumerGroupPrefix,
        config.topic(),
        config.filter(),
        consumerProperties, messageClass);
    consumers.put(consumer.getConsumerGroupId(), consumer);
    return consumer;
  }

  public void unsubscribeAllTopics() {
    consumers.values().stream()
        .filter(SerumKafkaConsumer::isSubscribed)
        .forEach(SerumKafkaConsumer::unsubscribe);

    deleteEmptyConsumerGroups();
  }

  public void closeProducer() {
    if (producer != null) {
      producer.close();
      producer = null;
    }
  }

  public RecordMetadata publishMessage(String topic, String value) {
    var producerRecord = new ProducerRecord<String, String>(topic, value);
    return publishMessage(producerRecord);
  }

  public RecordMetadata publishMessage(ProducerRecord<String, String> producerRecord) {
    log.info("Published message on topic {}. Message: {}", producerRecord.topic(), producerRecord.value());
    try {
      return getProducer().send(producerRecord).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new KafkaClientException("Error when publishing message to topic " + producerRecord.topic(), e);
    }
  }

  public void deleteEmptyConsumerGroups() {
    try (AdminClient adminClient = AdminClient.create(adminProperties)) {
      var emptyConsumerGroupsIds = adminClient.listConsumerGroups().all().get().stream()
          .filter(consumerGroup -> consumerGroup.groupId().startsWith(consumerGroupPrefix))
          .filter(consumerGroup -> consumerGroup.state().isPresent())
          .filter(consumerGroup -> consumerGroup.state().orElseThrow().equals(ConsumerGroupState.EMPTY))
          .map(ConsumerGroupListing::groupId)
          .peek(groupId -> log.info("Deleting empty consumer group: {}", groupId))
          .collect(Collectors.toList());

      adminClient.deleteConsumerGroups(emptyConsumerGroupsIds).all().get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }

  }

  private synchronized KafkaProducer<String, String> getProducer() {
    if (producer == null) {
      producer = new KafkaProducer<>(producerProperties);
    }
    return producer;
  }

  public static class Builder {

    private final Properties consumerProperties;
    private final Properties adminProperties;
    private final Properties producerProperties;
    private final String bootstrapServers;

    public Builder(String bootstrapServers) {
      this.consumerProperties = getDefaultConsumerProperties(bootstrapServers);
      this.adminProperties = getDefaultAdminProperties(bootstrapServers);
      this.producerProperties = getDefaultProducerProperties(bootstrapServers);
      this.bootstrapServers = bootstrapServers;
    }

    public Builder withConsumerProperty(String key, String value) {
      this.consumerProperties.setProperty(key, value);
      return this;
    }

    public Builder withAdminProperty(String key, String value) {
      this.adminProperties.setProperty(key, value);
      return this;
    }

    public SerumKafkaClient build() {
      return new SerumKafkaClient(bootstrapServers, consumerProperties, adminProperties, producerProperties);
    }
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
