package xdean.junit.ex.paramv2;

import static xdean.jex.util.lang.ExceptionUtil.uncheck;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import xdean.jex.util.lang.PrimitiveTypeUtil;
import xdean.jex.util.reflect.ReflectUtil;
import xdean.junit.ex.paramv2.annotation.GroupBy;
import xdean.junit.ex.paramv2.annotation.GroupBy.Group;
import xdean.junit.ex.paramv2.annotation.ParamTest;

public class ParamTestRunner<P> extends BlockJUnit4ClassRunner {

  private Class<P> paramClass;
  private List<Param<P>> params;
  private Map<Param<P>, Description> paramDescriptions;
  private Map<FrameworkMethod, Description> testDescriptions;
  private boolean groupByParam;
  private Description rootDescription;

  public ParamTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    paramDescriptions = new ConcurrentHashMap<>();
    testDescriptions = new ConcurrentHashMap<>();
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    if (!initParamClass(errors)) {
      return;
    }
    super.collectInitializationErrors(errors);
    validateParamGetter(errors);
    initGroupBy();
    initParams();
  }

  /**
   * Parameter provider method must be {@code public static Param/List<Param> methodName();}<br>
   * Parameter provider field must be {@code Param/List<Param> fieldName;} TODO array
   *
   * @param errors
   */
  protected void validateParamGetter(List<Throwable> errors) {
    getTestClass().getAnnotatedMethods(xdean.junit.ex.paramv2.annotation.Param.class).forEach(m -> {
      Method method = m.getMethod();
      if (!Modifier.isStatic(method.getModifiers())) {
        errors.add(new Exception("Param Getter Method " + m.getName() + " should be static."));
      }
      if (!Modifier.isPublic(method.getModifiers())) {
        errors.add(new Exception("Param Getter Method " + m.getName() + " should be public."));
      }
      if (method.getParameterCount() != 0) {
        errors.add(new Exception("Param Getter Method " + m.getName() + " should has no argument."));
      }
      if (!isParamProviderType(method.getGenericReturnType())) {
        errors.add(new Exception("Param Getter Method " + m.getName() + " should return Param or List<Param>."));
      }
    });
    getTestClass().getAnnotatedFields(xdean.junit.ex.paramv2.annotation.Param.class).forEach(f -> {
      Field field = f.getField();
      if (!Modifier.isStatic(field.getModifiers())) {
        errors.add(new Exception("Param Getter Field " + f.getName() + " should be static."));
      }
      if (!Modifier.isPublic(field.getModifiers())) {
        errors.add(new Exception("Param Getter Field " + f.getName() + " should be public."));
      }
      if (!Modifier.isFinal(field.getModifiers())) {
        errors.add(new Exception("Param Getter Field " + f.getName() + " should be final."));
      }
      if (!isParamProviderType(field.getGenericType())) {
        errors.add(new Exception("Param Getter Field " + f.getName() + " should be Param or List<Param>."));
      }
    });
  }

  protected boolean isParamProviderType(Type type) {
    if (type instanceof Class && Objects.equals(PrimitiveTypeUtil.toWrapper((Class<?>) type), getParamClass())) {
      return true;
    }
    Class<?>[] genericTypes = ReflectUtil.getGenericTypes(type, List.class);
    return genericTypes.length == 1 && Objects.equals(genericTypes[0], getParamClass());
  }

  protected void initGroupBy() {
    GroupBy annotation = getTestClass().getAnnotation(GroupBy.class);
    if (annotation != null) {
      groupByParam = annotation.value() == Group.PARAM;
    }
  }

  private void initParams() {
    params = getParamValues().stream()
        .map(param -> new Param<>(getParamDisplayName(param), param))
        .collect(Collectors.toList());
  }

  /**
   * First, from subclass definition.<br>
   * Second, from method definition.
   *
   * @param errors
   * @return
   */
  @SuppressWarnings("unchecked")
  protected boolean initParamClass(List<Throwable> errors) {
    Class<?>[] genericTypes = ReflectUtil.getGenericTypes(this.getClass(), ParamTestRunner.class);
    if (genericTypes.length == 0 || genericTypes[0] == null) {
      Class<?>[] classes = computeTestMethods().stream()
          .filter(m -> m.getMethod().getParameterCount() == 1)
          .map(m -> m.getMethod().getParameterTypes()[0])
          .distinct()
          .toArray(Class<?>[]::new);
      if (classes.length != 1) {
        errors.add(new InitializationError("Con't infer the parameter type."));
        return false;
      } else {
        paramClass = (Class<P>) PrimitiveTypeUtil.toWrapper(classes[0]);
      }
    } else {
      paramClass = (Class<P>) genericTypes[0];
    }
    return true;
  }

  /******************************************************************************/

  /**
   * First, List field named "param".<br>
   * Second, return List method named "param"<br>
   *
   * Can overrride.
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  protected List<P> getParamValues() {
    List<Object> list = new ArrayList<>();
    getTestClass().getAnnotatedFields(xdean.junit.ex.paramv2.annotation.Param.class)
        .forEach(f -> list.add(uncheck(() -> f.get(null))));
    getTestClass().getAnnotatedMethods(xdean.junit.ex.paramv2.annotation.Param.class)
        .forEach(m -> list.add(uncheck(() -> m.invokeExplosively(null))));
    return list.stream().flatMap(param -> {
      if (getParamClass().isInstance(param)) {
        return Stream.of((P) param);
      } else {
        return ((List<P>) param).stream();
      }
    }).collect(Collectors.toList());
  }

  @Override
  public Description getDescription() {
    if (rootDescription == null) {
      rootDescription = Description.createSuiteDescription(getName(), getRunnerAnnotations());
      List<FrameworkMethod> tests = getChildren();
      List<Param<P>> params = getParams();
      params.forEach(param -> {
        Description suite = Description.createSuiteDescription(param.getName());
        paramDescriptions.put(param, suite);
        tests.forEach(test -> suite.addChild(param.getDescription(test, getTestDisplayName(test, param))));
        if (groupByParam) {
          rootDescription.addChild(suite);
        }
      });
      tests.forEach(test -> {
        Description suite = Description.createSuiteDescription(test.getName());
        testDescriptions.put(test, suite);
        params.forEach(param -> suite.addChild(param.getDescription(test, getTestDisplayName(test, param))));
        if (!groupByParam) {
          rootDescription.addChild(suite);
        }
      });
    }
    return rootDescription;
  }

  @Override
  protected Description describeChild(FrameworkMethod test) {
    return testDescriptions.get(test);
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    params.forEach(param -> {
      Description description = param.getDescription(method);
      if (isIgnored(method)) {
        notifier.fireTestIgnored(description);
      } else {
        runLeaf(methodBlock(method, param), description, notifier);
      }
    });
  }

  protected Statement methodBlock(FrameworkMethod method, Param<P> param) {
    Object test;
    try {
      test = Util.reflectCall(this::createTest);
    } catch (Throwable e) {
      return new Fail(e);
    }

    Statement statement = methodInvoker(method, test, param);
    statement = possiblyExpectingExceptions(method, test, statement);
    statement = withPotentialTimeout(method, test, statement);
    statement = withBefores(method, test, statement);
    statement = withAfters(method, test, statement);
    // statement = withRules(method, test, statement); TODO
    return statement;
  }

  protected Statement methodInvoker(FrameworkMethod method, Object test, Param<P> param) {
    return Util.statement(() -> method.invokeExplosively(test, param.getValue()));
  }

  /**
   * Actually is validatePublicVoid(NoArg/Param)Methods
   */
  @Override
  protected void validatePublicVoidNoArgMethods
      (Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
    for (FrameworkMethod eachTestMethod : methods) {
      eachTestMethod.validatePublicVoid(isStatic, errors);
      Method method = eachTestMethod.getMethod();
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length != 1) {
        errors.add(new Exception("Method " + method.getName() + " should have no parameters"));
      }
      // The only parameter must be the param type.
      else if (!Objects.equals(PrimitiveTypeUtil.toWrapper(parameterTypes[0]), getParamClass())) {
        errors.add(new Exception("Method " + method.getName() + " don't have the correct param type"));
      }
    }
  }

  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    return getTestClass().getAnnotatedMethods(Test.class);
  }

  protected List<FrameworkMethod> computeParamTestMethods() {
    return getTestClass().getAnnotatedMethods(ParamTest.class);
  }

  /******************************************************************/

  protected String getTestDisplayName(FrameworkMethod method, Param<P> param) {
    ParamTest nameAnno = method.getAnnotation(ParamTest.class);
    return nameAnno == null ? method.getName() : nameAnno.value().replace("$", param.getName());
  }

  protected String getParamDisplayName(P param) {
    return param.toString();
  }

  public List<Param<P>> getParams() {
    return params;
  }

  public Class<P> getParamClass() {
    return paramClass;
  }
}
