package pl.net.testit.serum.reporting.extension;

import io.qameta.allure.Allure;
import io.qameta.allure.listener.StepLifecycleListener;
import io.qameta.allure.model.StepResult;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;


public class LogHandler implements Extension, StepLifecycleListener, AfterTestExecutionCallback {

  private final LogRepository logRepository = LogRepository.getInstance();

  @Override
  public void beforeStepStop(StepResult result) {
    logRepository.getAndClearLogs().forEach(logEntry -> {
      Allure.getLifecycle().addAttachment(logEntry.getHeader(), "text/plain", "txt", logEntry.getContent().getBytes());
    });
  }

  @Override
  public void afterTestExecution(ExtensionContext context) {
    logRepository.getAndClearLogs();
    Allure.getLifecycle().stopStep();
  }
}
