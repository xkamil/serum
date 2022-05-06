package pl.net.testit.serum.reporting.extension;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import pl.net.testit.serum.reporting.BaseTestSuite;

public class TestReportAppender extends OutputStreamAppender<ILoggingEvent> {

  // skip adding logs for those loggers to allure report.
  private static final List<String> SKIPP_LOGGERS = List.of(
      "IssuesListExtension",
      "LoggingExtensions",
      BaseTestSuite.class.getSimpleName(),
      AllureLifecycle.class.getSimpleName()
  );
  private final Level logLevel = Level.INFO;
  private final LogRepository logRepository = LogRepository.getInstance();
  private final LoggerContext loggerContext = new LoggerContext();

  public TestReportAppender() {
    this.setEncoder(getDefaultEncoder());
    this.setOutputStream(new ByteArrayOutputStream());
    this.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    var loggerName = event.getLoggerName();
    var logMessage = formatMessage(event);

    if (!event.getLevel().isGreaterOrEqual(logLevel)) {
      return;
    }

    if (SKIPP_LOGGERS.stream().anyMatch(loggerName::contains)) {
      return;
    }

    if (loggerName.contains("RequestLoggingFilter")) {
      var header = "HTTP" + StringUtils.substringBetween(logMessage, "HTTP", "\n").trim();
      Allure.getLifecycle().addAttachment(header, "text/plain", "txt", logMessage.getBytes(StandardCharsets.UTF_8));
      logRepository.addLog("logs", header);
      return;
    }

    if (loggerName.contains("ResponseLoggingFilter")) {
      var header = "HTTP" + StringUtils.substringBetween(logMessage, "HTTP", "\n").trim();
      Allure.getLifecycle().addAttachment(header, "text/plain", "txt", logMessage.getBytes(StandardCharsets.UTF_8));
      logRepository.addLog("logs", header);
      return;
    }

    logRepository.addLog("logs", logMessage);
  }

  private PatternLayoutEncoder getDefaultEncoder() {
    var encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    encoder.setPattern("%d{HH:mm:ss.SSS} [%-5level] %c{1.} - %msg%n%rEx");
    encoder.start();
    return encoder;
  }

  private String formatMessage(ILoggingEvent event) {
    return String.format("%s %s %s - %s", Instant.ofEpochMilli(event.getTimeStamp()), event.getLevel(),
        event.getLoggerName().replaceAll("\\B\\w+(\\.[a-zA-Z])", "$1"), event.getFormattedMessage());
  }

}
