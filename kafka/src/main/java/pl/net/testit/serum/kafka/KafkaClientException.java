package pl.net.testit.serum.kafka;

public class KafkaClientException extends RuntimeException{

  public KafkaClientException(String message, Throwable ex) {
    super(message, ex);
  }
}
