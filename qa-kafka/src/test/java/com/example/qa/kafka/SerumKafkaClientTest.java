package com.example.qa.kafka;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.serum.commons.json.JsonEntity;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;


public class SerumKafkaClientTest {

  public static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));
  public static SerumKafkaClient kafkaClient;
  private static final String TOPIC_1 = "TOPIC_1";

  @BeforeAll
  static void beforeAll() throws Exception {
    kafka.start();
    var bootstrapServers = kafka.getBootstrapServers();
    kafkaClient = SerumKafkaClient.builder(bootstrapServers).build();
    createTopics(bootstrapServers, TOPIC_1);
  }

  @AfterAll
  static void afterAll() throws Exception {
    kafkaClient.closeProducer();
    kafkaClient.unsubscribeAllTopics();
  }

  @Test
  void kafkaClientTest() {
    // given message published before client subscribe topic
    var message1 = "{'type':'type1','number':1}".replace("'", "\"");
    kafkaClient.publishEvent(TOPIC_1, message1);

    // when client subscribe to topic
    var event1Consumer = kafkaClient.subscribe(Event1.class);
    var event2Consumer = kafkaClient.subscribe(Event2.class);

    // and messages are published
    var message2 = "{'type':'type2','number':2}".replace("'", "\"");
    var message3 = "{'owner':'Dog'}".replace("'", "\"");

    kafkaClient.publishEvent(TOPIC_1, message2);
    kafkaClient.publishEvent(TOPIC_1, message3);

    // then message published before subscription should not be found
    assertThrows(ConditionTimeoutException.class, () -> {
      event1Consumer.waitForEvent(5, f -> f.getContent().type.equals("type1"));
    });

    // and messages published after subscription should be found
    var event2 = event1Consumer.waitForEvent(5, f -> true);
    var event3 = event2Consumer.waitForEvent(5, f -> f.getContent().owner.equals("Dog"));

    assertAll(
        () -> assertThat(event2.getContent().type).isEqualTo("type2"),
        () -> assertThat(event2.getContent().number).isEqualTo(2),
        () -> assertThat(event3.getContent().owner).isEqualTo("Dog")
    );

  }

  @KafkaEvent(topic = TOPIC_1, filter = "type")
  public static class Event1 extends JsonEntity {

    public String type;
    public Integer number;
  }

  @KafkaEvent(topic = TOPIC_1, filter = "owner")
  public static class Event2 extends JsonEntity {

    public String owner;
  }

  private static void createTopics(String bootstrapServers, String... topicNames) throws Exception {
    var props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);

    try (Admin admin = Admin.create(props)) {
      int partitions = 5;
      short replicationFactor = 1;
      var topics = Arrays.stream(topicNames)
          .map(topicName -> new NewTopic(topicName, partitions, replicationFactor))
          .collect(Collectors.toList());
      var result = admin.createTopics(topics);
      result.all().get();
    }
  }
}
