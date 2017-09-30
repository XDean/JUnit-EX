package xdean.junit.ex.param;

import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import xdean.jex.util.reflect.FunctionInterfaceUtil;
import xdean.jex.util.reflect.ReflectUtil;
import xdean.junit.ex.ParamTest.ReflectParamTest;

public abstract class ParamTestRunner<P> extends ParentRunner<Param<P>> {

  List<Param<P>> params;
  List<ParamMethodTest<P>> tests;
  Map<Param<P>, Description> descriptionMap;
  Class<P> paramClass;

  public ParamTestRunner(Class<?> clz) throws InitializationError {
    super(clz);
    descriptionMap = new ConcurrentHashMap<>();
    tests = new ArrayList<>();
    params = initParams();
    validate();
    initParamClass();
    initTests();
  }

  private void validate() {
    // see ParentRunner
    // 1. @Test not static
    // 2. has default constructor
  }

  private List<Param<P>> initParams() {
    return getParams().stream()
        .map(p -> new Param<>(getParamName(p), p))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private void initParamClass() throws InitializationError {
    Class<?>[] genericTypes = ReflectUtil.getGenericTypes(this.getClass(), ParamTestRunner.class);
    if (genericTypes[0] == null) {
      throw new InitializationError("Children must specify T class.");
    }
    paramClass = (Class<P>) genericTypes[0];
  }

  @SuppressWarnings("unchecked")
  private void initTests() {
    getTestClass().getAnnotatedMethods(Test.class)
        .forEach(
            fm -> {
              ReflectParamTest<P> test = FunctionInterfaceUtil.methodToFunctionInterface(fm.getMethod(),
                  uncheck(() -> getTestClass().getOnlyConstructor().newInstance()), ReflectParamTest.class,
                  getParamClass());
              if (test != null) {
                tests.add(new ParamMethodTest<>(fm, test));
              }
            });
  }

  @Override
  protected List<Param<P>> getChildren() {
    return params;
  }

  @Override
  protected final Description describeChild(Param<P> child) {
    return describeParam(child);
  }

  protected Description describeParam(Param<P> param) {
    Description description = descriptionMap.get(param);
    if (description == null) {
      Description suite = Description.createSuiteDescription(param.getName());
      tests.forEach(t -> suite.addChild(t.getDescription(param)));
      description = suite;
      descriptionMap.put(param, description);
    }
    return description;
  }

  @Override
  protected final void runChild(Param<P> child, RunNotifier notifier) {
    runParam(child, notifier);
  }

  protected void runParam(Param<P> param, RunNotifier notifier) {
    if (isIgnored(param)) {
      notifier.fireTestIgnored(describeChild(param));
    }
    tests.forEach(test -> {
      Description description = test.getDescription(param);
      if (isIgnored(test)) {
        notifier.fireTestIgnored(description);
      } else {
        notifier.fireTestStarted(description);
        run(param, test, notifier);
      }
    });
  }

  protected void run(Param<P> param, ParamMethodTest<P> test, RunNotifier notifier) {
    Description description = test.getDescription(param);
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
    try {
      test.run(param);
    } catch (AssumptionViolatedException e) {
      eachNotifier.addFailedAssumption(e);
    } catch (Throwable e) {
      eachNotifier.addFailure(e);
    } finally {
      eachNotifier.fireTestFinished();
    }
  }

  @Override
  protected boolean isIgnored(Param<P> child) {
    return tests.stream().allMatch(this::isIgnored);
  }

  protected boolean isIgnored(ParamMethodTest<P> test) {
    return test.getMethod().getAnnotation(Ignore.class) != null;
  }

  /********************** Getter ***************************/
  protected String getParamName(P param) {
    return param.toString();
  }

  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return new TestClass(testClass);
  }

  protected final Class<P> getParamClass() {
    return paramClass;
  }

  protected abstract List<P> getParams();

  @Override
  protected String getName() {
    return getTestClass().getName();
  }

  @Override
  protected Annotation[] getRunnerAnnotations() {
    return getTestClass().getAnnotations();
  }
}
