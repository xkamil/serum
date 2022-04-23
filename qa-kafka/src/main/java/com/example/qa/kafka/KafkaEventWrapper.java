package com.example.qa.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class KafkaEventWrapper<T> {

  private final ConsumerRecord<String, String> consumerRecord;
  private final T content;

  public KafkaEventWrapper(ConsumerRecord<String, String> consumerRecord, T content) {
    this.consumerRecord = consumerRecord;
    this.content = content;
  }

  public ConsumerRecord<String, String> getConsumerRecord() {
    return consumerRecord;
  }

  public T getContent() {
    return content;
  }
}
