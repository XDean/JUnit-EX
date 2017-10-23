package xdean.junit.ex.param;

import java.util.Comparator;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.google.common.collect.LinkedListMultimap;

import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.GroupBy.Group;

public class ParamSuite extends Suite {

  public ParamSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
    super(klass, builder);
  }

  Description rootDescription, otherDescription;
  boolean groupByParam = true;
  LinkedListMultimap<Param<?>, ParamTestRunner<?>> paramTests;

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    super.collectInitializationErrors(errors);
    if (errors.isEmpty()) {
      initGroupBy();
    }
  }

  protected void initGroupBy() {
    GroupBy annotation = getTestClass().getAnnotation(GroupBy.class);
    if (annotation != null) {
      groupByParam = annotation.value() == Group.PARAM;
    }
  }

  @Override
  protected void runChild(Runner runner, RunNotifier notifier) {
    super.runChild(runner, notifier);
  }

  @Override
  public Description getDescription() {
    initDescription();
    return rootDescription;
  }

  private void initDescription() {
    if (rootDescription == null) {
      rootDescription = Description.createSuiteDescription(getName(), getRunnerAnnotations());
      otherDescription = Description.createSuiteDescription("other");
      paramTests = LinkedListMultimap.create();
      for (Runner child : getChildren()) {
        if (child instanceof ParamTestRunner) {
          ParamTestRunner<?> ptr = (ParamTestRunner<?>) child;
          Description ptrOther = ptr.getOtherDescription();
          if (!ptrOther.isEmpty()) {
            Description ptrOtherSuite = Description.createSuiteDescription(ptr.getName(), ptr.getRunnerAnnotations());
            ptrOther.getChildren().forEach(ptrOtherSuite::addChild);
            otherDescription.addChild(ptrOtherSuite);
          }
          ptr.getParams().forEach(p -> paramTests.put(p, ptr));
        } else if (child instanceof ParamSuite) {
          ParamSuite ps = (ParamSuite) child;
          ps.initDescription();
          if (!ps.otherDescription.isEmpty()) {
            Description ptrOtherSuite = Description.createSuiteDescription(ps.getName(), ps.getRunnerAnnotations());
            ps.otherDescription.getChildren().forEach(ptrOtherSuite::addChild);
            otherDescription.addChild(ptrOtherSuite);
          }
          this.paramTests.putAll(ps.paramTests);
        } else {
          otherDescription.addChild(super.describeChild(child));
        }
      }
      if (groupByParam) {
        paramTests.keySet()
            .stream()
            .sorted(Comparator.comparing(Param::getName))
            .forEach(param -> {
              List<ParamTestRunner<?>> paramRunners = paramTests.get(param);
              Description paramSuite = param.getDescription();
              paramRunners.forEach(runner -> {
                List<FrameworkMethod> tests = runner.computeParamTestMethods();
                if (!tests.isEmpty()) {
                  Description runnerSuite = Description.createSuiteDescription(runner.getName(),
                      param.getValue() + "-" + runner.getName());
                  tests.forEach(test -> runnerSuite.addChild(
                      param.getDescription(test, runner.getTestDisplayName(test, param, groupByParam))));
                  paramSuite.addChild(runnerSuite);
                }
              });
              if (!paramSuite.isEmpty()) {
                rootDescription.addChild(paramSuite);
              }
            });
      } else {

      }
      if (!otherDescription.isEmpty()) {
        rootDescription.addChild(otherDescription);
      }
    }
  }

  @Override
  protected List<Runner> getChildren() {
    return super.getChildren();
  }

  @Override
  protected Description describeChild(Runner child) {
    throw new UnsupportedOperationException();
  }
}
