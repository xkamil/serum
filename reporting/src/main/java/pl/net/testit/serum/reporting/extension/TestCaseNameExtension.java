package pl.net.testit.serum.reporting.extension;

import io.qameta.allure.Allure;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.params.ParameterizedTest;

public class TestCaseNameExtension implements AfterEachCallback, BeforeEachCallback, TestWatcher {

  @Override
  public void afterEach(ExtensionContext context) {
    Allure.getLifecycle().updateTestCase(u -> {
      var issues = u.getLinks().stream()
          .filter(l -> l.getType().equals("issue"))
          .map(l -> "[" + l.getName() + "] ")
          .collect(Collectors.joining());
      u.setName(issues + u.getName());
      u.setFullName(formatFullName(u.getFullName()));
    });
  }

  @Override
  public void testAborted(ExtensionContext context, Throwable cause) {
    Allure.getLifecycle().updateTestCase(u -> {
      u.setName("ABORTED " + u.getName());
    });
  }

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    var testClass = extensionContext.getTestClass().orElseThrow();
    var testClassDisplayName = Arrays.stream(testClass.getAnnotationsByType(DisplayName.class)).findFirst();
    testClassDisplayName.ifPresent(dn -> Allure.feature(dn.value()));

    var testMethod = extensionContext.getTestMethod().orElseThrow();
    var displayName = Arrays.stream(testMethod.getAnnotationsByType(DisplayName.class)).findFirst();
    var parameterizedDisplayName = Arrays.stream(testMethod.getAnnotationsByType(ParameterizedTest.class)).findFirst();
    displayName.ifPresent(dn -> parameterizedDisplayName.ifPresent(pn -> Allure.story(dn.value())));
  }

  private static String formatFullName(String fullName) {
    int start = fullName.lastIndexOf(".");
    var sb = new StringBuilder();
    sb.append(fullName.substring(0, start));
    sb.append("#");
    sb.append(fullName.substring(start + 1));
    return sb.toString();
  }
}