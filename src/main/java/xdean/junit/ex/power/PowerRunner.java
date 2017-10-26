package xdean.junit.ex.power;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

import xdean.jex.util.reflect.AnnotationUtil;
import xdean.junit.ex.power.annotation.ActualRunWith;

public class PowerRunner extends Runner {

  private TestClass testClass;
  private Runner actual;

  public PowerRunner(Class<?> clz, RunnerBuilder runnerBuilder) {
    testClass = new TestClass(clz);
    calcActualRunner(clz, runnerBuilder);
  }

  private void calcActualRunner(Class<?> clz, RunnerBuilder runnerBuilder) {
    ActualRunWith runWith = testClass.getAnnotation(ActualRunWith.class);
    if (runWith == null) {

      actual = runnerBuilder.safeRunnerForClass(clz);
    }
  }

  @Override
  public Description getDescription() {
    return actual.getDescription();
  }

  @Override
  public void run(RunNotifier notifier) {
    actual.run(notifier);
  }
}
