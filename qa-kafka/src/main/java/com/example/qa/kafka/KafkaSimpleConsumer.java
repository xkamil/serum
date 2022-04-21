package com.example.qa.kafka;

import static org.awaitility.Awaitility.await;

import com.example.serum.commons.json.JsonParser;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaSimpleConsumer<T> {

  private static final Logger log = LoggerFactory.getLogger(KafkaSimpleConsumer.class);
  private final String topic;
  private final String filter;
  private final Properties properties;
  private final ExecutorService executorService;
  private final Class<T> eventContentClass;
  private final BiFunction<String, Class<T>, T> eventContentDeserializer;
  private final List<KafkaEventWrapper<T>> events = Collections.synchronizedList(new ArrayList<>(100));
  private long skipEventsCreatedAfterSubscription;
  private boolean logAllEvents;
  private boolean subscribed;
  private volatile KafkaConsumer<String, String> kafkaTopicConsumer;

  public KafkaSimpleConsumer(ExecutorService executorService, String topic, String filter, Properties properties,
      Class<T> eventContentClass) {
    this.topic = topic;
    this.filter = filter;
    this.properties = properties;
    this.executorService = executorService;
    this.logAllEvents = true;
    this.eventContentClass = eventContentClass;
    this.eventContentDeserializer = (eventContent, targetClass) -> JsonParser.fromJSON(eventContent, eventContentClass);
  }

  public KafkaSimpleConsumer<T> setLogAllEvents(boolean logAllEvents) {
    this.logAllEvents = logAllEvents;
    return this;
  }

  public KafkaSimpleConsumer<T> unsubscribe() {
    subscribed = false;
    return this;
  }

  public KafkaSimpleConsumer<T> subscribe() {
    if (subscribed) {
      return this;
    }
    subscribed = true;

    skipEventsCreatedAfterSubscription = System.currentTimeMillis() - 5_000;
    log.info("Subscribed topic {}", topic);

    executorService.submit(() -> {
      kafkaTopicConsumer = new KafkaConsumer<>(properties);
      kafkaTopicConsumer.subscribe(List.of(topic));

      while (subscribed) {
        var consumerRecords = kafkaTopicConsumer.poll(Duration.of(100, ChronoUnit.MILLIS));
        kafkaTopicConsumer.commitSync();
        consumerRecords.forEach(this::processRecord);
      }
      kafkaTopicConsumer.unsubscribe();
    });
    return this;
  }

  public KafkaEventWrapper<T> waitForEvent(int durationInSeconds, Predicate<KafkaEventWrapper<T>> filter) {
    log.info("Waiting {} seconds for matching event on topic: {}", durationInSeconds, topic);
    return await().timeout(Duration.ofSeconds(durationInSeconds)).pollInterval(Duration.ofMillis(100)).until(
        () -> events.stream().filter(filter).findFirst(),
        Optional::isPresent
    ).orElseThrow();
  }

  public List<KafkaEventWrapper<T>> waitForEvents(int durationInSeconds, int count,
      Predicate<KafkaEventWrapper<T>> filter) {
    log.info("Waiting {} seconds for matching {} events on topic: {}", durationInSeconds, count, topic);
    return await().timeout(Duration.ofSeconds(durationInSeconds)).pollInterval(Duration.ofMillis(100)).until(
        () -> events.stream().filter(filter).collect(Collectors.toList()),
        events -> events.size() == count
    );
  }

  private void processRecord(ConsumerRecord<String, String> record) {
    if (record.timestamp() < skipEventsCreatedAfterSubscription) {
      return;
    }

    if (logAllEvents) {
      log.info("New event on topic {}:\n headers: {}\n content: {}", topic,
          parseHeaders(record.headers()), record.value());
    }

    if (!record.value().contains(filter)) {
      return;
    }

    try {
      T deserializedMessage = eventContentDeserializer.apply(record.value(), eventContentClass);
      var eventWrapper = new KafkaEventWrapper<>(record, deserializedMessage);
      events.add(eventWrapper);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  private String parseHeaders(Headers headers) {
    return StreamSupport.stream(headers.spliterator(), false)
        .map(header -> String.format("%s=%s, ", header.key(), new String(header.value(), StandardCharsets.UTF_8)))
        .collect(Collectors.joining());
  }
}
