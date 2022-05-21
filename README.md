
# Serum - qa framework

## Installation

This framework contains multiple modules. In order to use specific module fallow module installation guide.


### 1.  kafka module

Module contains kafka client that allows consuming and producing messages to kafka topics.

#### Installation:
In pom xml add
```xml
<dependency>
	<groupId>pl.net.testit.serum</groupId>  
	<artifactId>serum</artifactId>  
	<version>LATEST_VERSION</version>
</dependency>
```

#### Customization

Apache kafka client has very verbose logs so in order to show only important logs add this in logback.xml 

```xml
<logger name="org.apache.kafka" level="WARN" />
```

#### Example usage

```java
// Define class that will be used to deserialize consumed messages from topic TOPIC_NAME. Messages not containing text: "type" will be skipped
@KafkaEvent(topic = TOPIC_NAME, filter = "type")  
public static class Event1 extends JsonEntity {  
	public String owner; 
	public String type;
} 

// Build kafka client
var kafkaClient = SerumKafkaClient.builder(bootstrapServers).build();

// Subscribe to topic
var event1Consumer = kafkaClient.subscribe(Event1.class);

// wait 5 seconds for event that has owner = "John"
var event3 = event2Consumer.waitForEvent(5, f -> f.getContent().owner.equals("John"));
```

### 2.  reporting module