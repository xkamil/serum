package pl.net.testit.serum.kafka;

import static org.awaitility.Awaitility.await;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.record.TimestampType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.net.testit.serum.commons.json.JsonParser;

public class SerumKafkaConsumer<T> {

  private static final Logger log = LoggerFactory.getLogger(SerumKafkaConsumer.class);
  private final String topic;
  private final String filter;
  private final Properties properties;
  private final ExecutorService executorService;
  private final Class<T> eventContentClass;
  private final BiFunction<String, Class<T>, T> eventContentDeserializer;
  private final List<KafkaEventWrapper<T>> events = Collections.synchronizedList(new ArrayList<>(100));
  private final String consumerGroupId;
  private final long subscribedAt;
  private boolean logAllEvents;
  private volatile boolean subscribed;

  public SerumKafkaConsumer(
      ExecutorService executorService,
      String consumerGroupPrefix,
      String topic,
      String filter,
      Properties properties,
      Class<T> eventContentClass) {
    this.topic = topic;
    this.filter = filter;
    this.properties = (Properties) properties.clone();
    this.consumerGroupId = consumerGroupPrefix + UUID.randomUUID();
    this.properties.put("group.id", consumerGroupId);
    this.executorService = executorService;
    this.logAllEvents = true;
    this.eventContentClass = eventContentClass;
    this.eventContentDeserializer = (eventContent, targetClass) -> JsonParser.fromJSON(eventContent, eventContentClass);

    subscribe();
    subscribedAt = System.currentTimeMillis();
  }

  public SerumKafkaConsumer<T> setLogAllEvents(boolean logAllEvents) {
    this.logAllEvents = logAllEvents;
    return this;
  }

  public SerumKafkaConsumer<T> unsubscribe() {
    subscribed = false;
    return this;
  }

  public KafkaEventWrapper<T> waitForEvent(int durationInSeconds, Predicate<KafkaEventWrapper<T>> filter) {
    log.info("Waiting {} seconds for matching event on topic: {}", durationInSeconds, topic);

    return await().timeout(Duration.ofSeconds(durationInSeconds)).pollInterval(Duration.ofMillis(100)).until(
        () -> events.stream().filter(filter).collect(Collectors.toList()),
        matchingEvents -> {
          log.info("Expected 1 event on topic {} matching filter. Found {}", topic, matchingEvents.size());
          return matchingEvents.size() == 1;
        }
    ).get(0);
  }

  public List<KafkaEventWrapper<T>> waitForEvents(int durationInSeconds, int count,
      Predicate<KafkaEventWrapper<T>> filter) {
    log.info("Waiting {} seconds for matching {} events on topic: {}", durationInSeconds, count, topic);

    return await().timeout(Duration.ofSeconds(durationInSeconds)).pollInterval(Duration.ofMillis(100)).until(
        () -> events.stream().filter(filter).collect(Collectors.toList()),
        matchingEvents -> {
          log.info("Expected 1 event on topic {} matching filter. Found {}", topic, matchingEvents.size());
          return matchingEvents.size() == count;
        }
    );

  }

  public boolean isSubscribed() {
    return subscribed;
  }

  public String getConsumerGroupId() {
    return consumerGroupId;
  }

  private SerumKafkaConsumer<T> subscribe() {
    subscribed = true;
    if (filter.isEmpty()) {
      log.info("Subscribed topic {}", topic);
    } else {
      log.info("Subscribed topic {} for events with content containing: {}", topic, filter);
    }

    executorService.submit(() -> {
      var kafkaTopicConsumer = new KafkaConsumer<String, String>(properties);
      kafkaTopicConsumer.subscribe(List.of(topic));
      while (subscribed) {
        var consumerRecords = kafkaTopicConsumer.poll(Duration.of(100, ChronoUnit.MILLIS));
        kafkaTopicConsumer.commitSync();
        consumerRecords.forEach(this::processRecord);
      }
      log.info("Unsubscribing topic {}", topic);
      kafkaTopicConsumer.unsubscribe();
    });

    return this;
  }

  private void processRecord(ConsumerRecord<String, String> record) {
    if (record.timestampType().equals(TimestampType.CREATE_TIME) && record.timestamp() < subscribedAt) {
      return;
    }

    if (!record.value().contains(filter)) {
      return;
    }

    if (logAllEvents) {
      log.info("New event on topic {}:\n headers: {}\n content: {}", topic,
          parseHeaders(record.headers()), record.value());
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
