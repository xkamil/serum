package com.example.serum.api.test;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTestSuite {

  private static final Logger logger = LoggerFactory.getLogger(BaseTestSuite.class);

  private String currentStepId;

  protected void _given(CharSequence message) {
    registerStep("given", message);
  }

  protected void _given(String message, Object... args) {
    _given(String.format(message, args));
  }

  protected void _when(CharSequence message) {
    registerStep("when", message);
  }

  protected void _when(String message, Object... args) {
    _when(String.format(message, args));
  }

  protected void _then(CharSequence message) {
    registerStep("then", message);
  }

  protected void _then(String message, Object... args) {
    _then(String.format(message, args));
  }

  protected void _and(CharSequence message) {
    registerStep("and", message);
  }

  protected void _and(String message, Object... args) {
    _and(String.format(message, args));
  }

  protected void _info(CharSequence message) {
    registerStep("info", message);
  }

  protected void _info(String message, Object... args) {
    _info(String.format(message, args));
  }

  private void registerStep(String prefix, CharSequence message) {
    if (null != currentStepId) {
      Allure.getLifecycle().stopStep(currentStepId);
    }
    currentStepId = UUID.randomUUID().toString();
    var stepResult = new StepResult().setName(String.format("%s: %s", prefix, message)).setStatus(Status.PASSED);
    Allure.getLifecycle().startStep(currentStepId, stepResult);
    logger.info("{}: {}", prefix, message);
  }
}
