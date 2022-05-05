package pl.net.testit.serum.reporting;

import ch.qos.logback.classic.LoggerContext;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.net.testit.serum.reporting.extension.LogHandler;
import pl.net.testit.serum.reporting.extension.TestCaseNameExtension;
import pl.net.testit.serum.reporting.extension.TestReportAppender;

@ExtendWith(TestCaseNameExtension.class)
@ExtendWith(LogHandler.class)
public abstract class BaseTestSuite {

  private final Logger logger = LoggerFactory.getLogger(BaseTestSuite.class);

  static {
    LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
    logCtx.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(new TestReportAppender());
  }

  private String currentStepId;

  protected void _given(String message, Object... args) {
    registerStep("given", message);
  }

  protected void _when(String message, Object... args) {
    registerStep("when", message);
  }

  protected void _then(String message, Object... args) {
    registerStep("then", message);
  }

  protected void _and(String message, Object... args) {
    registerStep("and", message);
  }

  protected void _info(String message, Object... args) {
    registerStep("info", message);
  }

  private void registerStep(String prefix, String message) {
    if (null != currentStepId) {
      Allure.getLifecycle().stopStep(currentStepId);
    }
    currentStepId = UUID.randomUUID().toString();
    var stepResult = new StepResult().setName(String.format("%s: %s", prefix, message)).setStatus(Status.PASSED);
    Allure.getLifecycle().startStep(currentStepId, stepResult);
    logger.info("{}: {}", prefix, message);
  }
}
