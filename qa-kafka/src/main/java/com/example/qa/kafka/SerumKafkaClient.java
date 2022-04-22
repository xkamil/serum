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
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.common.ConsumerGroupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SerumKafkaClient {

  private static final Logger log = LoggerFactory.getLogger(SerumKafkaClient.class);
  private static final ExecutorService executorService = Executors
      .newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

  private final Map<String, SerumKafkaConsumer<?>> consumers = new HashMap<>();
  private final String consumerGroupPrefix;
  private final Properties consumerProperties;
  private final Properties adminProperties;

  private SerumKafkaClient(String bootstrapServers, Properties consumerProperties, Properties adminProperties) {
    this.consumerProperties = consumerProperties;
    this.adminProperties = adminProperties;
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

  public static class Builder {

    private final Properties consumerProperties;
    private final Properties adminProperties;
    private final String bootstrapServers;

    public Builder(String bootstrapServers) {
      this.consumerProperties = getDefaultConsumerProperties(bootstrapServers);
      this.adminProperties = getDefaultAdminProperties(bootstrapServers);
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
      return new SerumKafkaClient(bootstrapServers, consumerProperties, adminProperties);
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

  private static Properties getDefaultConsumerProperties(String bootstrapServers) {
    var props = new Properties();
    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(CommonClientConfigs.SESSION_TIMEOUT_MS_CONFIG, "30000");
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("auto.offset.reset", "earliest");
    return props;
  }

  private static Properties getDefaultAdminProperties(String bootstrapServers) {
    var props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "50000");
    props.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "50000");
    return props;
  }

}
