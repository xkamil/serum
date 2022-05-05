package pl.net.testit.serum.reporting.extension;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import io.qameta.allure.AllureLifecycle;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import pl.net.testit.serum.reporting.BaseTestSuite;

public class TestReportAppender extends OutputStreamAppender<ILoggingEvent> {

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
    if (event.getLevel().isGreaterOrEqual(logLevel)) {
      String logMessage = formatMessage(event);
      String loggerName = event.getLoggerName();

      if (loggerName.contains("RequestLoggingFilter")) {
        var header = StringUtils.substringBetween(logMessage, "--request", "\\").trim();
        logRepository.addLog(header, logMessage);

      } else if (loggerName.contains("ResponseLoggingFilter")) {
        var header = "HTTP" + StringUtils.substringBetween(logMessage, "HTTP", "\n").trim();
        logRepository.addLog(header, logMessage);

      } else if (!(loggerName.contains("IssuesListExtension") || loggerName.contains("LoggingExtensions")
          || loggerName.contains(BaseTestSuite.class.getSimpleName()) || loggerName
          .contains(AllureLifecycle.class.getSimpleName()))) {
        logRepository.addLog("logs", logMessage);
      }
    }
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
