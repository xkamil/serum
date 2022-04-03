package com.example.serum.reporting;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTestSuite {

  private static final Logger logger = LoggerFactory.getLogger(BaseTestSuite.class);

  private String currentStepId;

  protected void _given(String message, Object... args) {
    registerStep("given", message);
  }

  protected void _when(String message, Object... args) {
    registerStep("when", message);
  }

  protected void _then(String message, Object... args) {
    registerStep("then", message);
    ;
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
