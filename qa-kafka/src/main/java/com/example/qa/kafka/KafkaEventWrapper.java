package com.example.qa.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class KafkaEventWrapper<T> {

  private final ConsumerRecord<String, String> event;
  private final T content;

  public KafkaEventWrapper(ConsumerRecord<String, String> event, T content) {
    this.event = event;
    this.content = content;
  }

  public ConsumerRecord<String, String> getEvent() {
    return event;
  }

  public T getContent() {
    return content;
  }
}
