package com.example.qa.kafka;

import com.example.serum.commons.json.JsonEntity;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;


public class SerumKafkaClient2Test {

  public static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));
  public static Producer<String, String> kafkaProducer;
  public static SerumKafkaClient kafkaClient;

  @BeforeAll
  static void beforeAll() throws ExecutionException, InterruptedException {
    kafka.start();

    Properties properties = new Properties();
    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

    try (Admin admin = Admin.create(properties)) {
      int partitions = 5;
      short replicationFactor = 1;
      var newTopic = new NewTopic("TOPIC_1", partitions, replicationFactor);
      var result = admin.createTopics(Collections.singleton(newTopic));
      var future = result.values().get("TOPIC_1");
      future.get();
    }

    var bootstrapServers = kafka.getBootstrapServers();
    kafkaProducer = getKafkaProducer(bootstrapServers);
    kafkaClient = SerumKafkaClient.builder(bootstrapServers)
        .withConsumerProperty("allow.auto.create.topics", "true")
        .build();
  }


  @Test
  void test() {

    var bookkeepingOrderStatusTopic = kafkaClient.subscribe(BookingSettledEvent.class).setLogAllEvents(true);

    Executors.newFixedThreadPool(1).submit(() -> {
      while (true) {
        Thread.sleep(1000);
        System.out.println("sending event");
        sendEvent("TOPIC_1", "{\"type\": \"PAYMENT_SETTLED\"}");
      }
    });

    var event = bookkeepingOrderStatusTopic.waitForEvents(100, 3, f -> true);

    bookkeepingOrderStatusTopic.unsubscribe();
  }

  private static void sendEvent(String topic, String eventContent) {
    var record = new ProducerRecord<String, String>(topic, eventContent);
    kafkaProducer.send(record);
  }

  private static Producer<String, String> getKafkaProducer(String bootstrapServers) {
    var props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    return new KafkaProducer<>(props);
  }

  @KafkaEvent(topic = "TOPIC_1")
  public static class BookingSettledEvent extends JsonEntity {

    public String type;
  }
}
