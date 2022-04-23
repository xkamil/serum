package com.example.qa.kafka;

public class KafkaClientException extends RuntimeException{

  public KafkaClientException(String message, Throwable ex) {
    super(message, ex);
  }
}
