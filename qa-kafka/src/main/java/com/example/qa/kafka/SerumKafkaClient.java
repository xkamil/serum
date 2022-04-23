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
  private static final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
      .setDaemon(true).build());

  private final String consumerGroupPrefix = "test-";
  private final Properties consumerProperties;
  private final Properties adminProperties;
  private final Properties producerProperties;
  private final Map<String, SerumKafkaConsumer<?>> consumers = new HashMap<>();
  private KafkaProducer<String, String> producer;

  SerumKafkaClient(Properties consumerProperties, Properties adminProperties, Properties producerProperties) {
    this.consumerProperties = consumerProperties;
    this.adminProperties = adminProperties;
    this.producerProperties = producerProperties;
  }

  public static SerumKafkaClientBuilder builder(String bootstrapServers) {
    return new SerumKafkaClientBuilder(bootstrapServers);
  }

  public <T> SerumKafkaConsumer<T> subscribe(Class<T> eventClass) {
    var config = Optional.ofNullable(eventClass.getAnnotation(KafkaEvent.class)).orElseThrow(
        () -> new RuntimeException("eventClass should be annotated by KafkaEvent")
    );
    var consumer = new SerumKafkaConsumer<T>(
        executorService,
        consumerGroupPrefix,
        config.topic(),
        config.filter(),
        consumerProperties, eventClass);
    consumers.put(consumer.getConsumerGroupId(), consumer);
    return consumer;
  }

  public RecordMetadata publishEvent(String topic, String value) {
    var producerRecord = new ProducerRecord<String, String>(topic, value);
    return publishEvent(producerRecord);
  }

  public RecordMetadata publishEvent(ProducerRecord<String, String> producerRecord) {
    log.info("Published event on topic {}. Event: {}", producerRecord.topic(), producerRecord.value());
    try {
      return getProducer().send(producerRecord).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new KafkaClientException("Error when publishing event to topic " + producerRecord.topic(), e);
    }
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

}
