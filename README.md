
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
	<artifactId>kafka</artifactId>  
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

#### Installation:

Add this dependency in your project:
```xml
<dependency>
	<groupId>pl.net.testit.serum</groupId>  
	<artifactId>reporing</artifactId>  
	<version>LATEST_VERSION</version>
</dependency>
```

Set this as parent in your project pom.xml:
```xml
  <groupId>pl.net.testit.serum</groupId>
  <artifactId>serum</artifactId>
  <packaging>pom</packaging>
  <version>LATEST_VERSION</version>
```

Add build plugins:
```xml
        <plugin>
          <groupId>io.qameta.allure</groupId>
          <artifactId>allure-maven</artifactId>
        </plugin>

        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
        </plugin>
```

If you decide not to set serum as parent of your project pom then in build plugins add full plugin configuration:
```xml
        <plugin>
          <groupId>io.qameta.allure</groupId>
          <artifactId>allure-maven</artifactId>
          <version>2.11.2</version>
          <configuration>
            <reportVersion>2.17.3</reportVersion>
          </configuration>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>3.0.0-M5</version>
          <dependencies>
            <dependency>
              <groupId>org.junit.jupiter</groupId>
              <artifactId>junit-jupiter-engine</artifactId>
              <version>5.6.2</version>
            </dependency>
            <dependency>
              <groupId>org.aspectj</groupId>
              <artifactId>aspectjweaver</artifactId>
              <version>1.9.6</version>
            </dependency>
          </dependencies>
          <configuration>
            <argLine>-javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/1.9.6/aspectjweaver-1.9.6.jar"</argLine>
            <properties>
              <property>
                <name>listener</name>
                <value>io.qameta.allure.junit5.AllureJunit5</value>
              </property>
            </properties>
            <systemProperties>
              <property>
                <name>allure.results.directory</name>
                <value>C:\projects\java\qa-serum\target/allure-results</value>
              </property>
              <property>
                <name>junit.jupiter.extensions.autodetection.enabled</name>
                <value>true</value>
              </property>
              <property>
                <name>allure.link.issue.pattern</name>
                <value>https://jira11{}</value>
              </property>
            </systemProperties>
          </configuration>
        </plugin>
```