package xdean.junit.ex.param;

import static xdean.jex.util.lang.ExceptionUtil.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import xdean.jex.extra.Pair;
import xdean.jex.util.lang.PrimitiveTypeUtil;
import xdean.jex.util.reflect.AnnotationUtil;
import xdean.jex.util.reflect.GenericUtil;
import xdean.junit.ex.friendly.FriendlyBlockJUnit4ClassRunner;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.GroupBy.Group;
import xdean.junit.ex.param.annotation.Param.ParamType;
import xdean.junit.ex.param.annotation.ParamTest;

public class ParamTestRunner<P> extends FriendlyBlockJUnit4ClassRunner {

  private Type paramType;
  private List<Param<P>> params;
  private Map<FrameworkMethod, Description> testDescriptions;
  private boolean groupByParam;
  private Description rootDescription;
  private Description other;

  public ParamTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
    testDescriptions = new ConcurrentHashMap<>();
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    validateParamTestExist(errors);
    if (!initParamClass(errors)) {
      return;
    }
    super.collectInitializationErrors(errors);
    validateParamGetter(errors);
    if (errors.isEmpty()) {
      initGroupBy();
      initParams();
    }
  }

  private void validateParamTestExist(List<Throwable> errors) {
    if (computeParamTestMethods().size() == 0) {
      errors.add(new Exception("There is no param test case!"));
      return;
    }
  }

  @Override
  protected void validateInstanceMethods(List<Throwable> errors) {
    validatePublicVoidNoArgMethods(After.class, false, errors);
    validatePublicVoidNoArgMethods(Before.class, false, errors);
    validateTestMethods(errors);

    if (computeTestMethods().size() == 0) {
      errors.add(new Exception("No runnable methods"));
    }
  }

  @Override
  protected void validateTestMethods(List<Throwable> errors) {
    validatePublicVoidNoArgMethods(Test.class, false, errors);
    validatePublicVoidParamMethods(ParamTest.class, false, errors);
  }

  /**
   * Parameter provider method must be {@code public static Param/Param[]/List<Param> methodName();}<br>
   * Parameter provider field must be {@code Param/Param[]/List<Param> fieldName;}
   *
   * @param errors
   */
  protected void validateParamGetter(List<Throwable> errors) {
    getTestClass().getAnnotatedMethods(xdean.junit.ex.param.annotation.Param.class).forEach(
        m -> {
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
          if (method.getParameterTypes().length != 0) {
            errors.add(new Exception("Param Getter Method " + m.getName() + " should not have generic type."));
          }
          xdean.junit.ex.param.annotation.Param anno = m.getAnnotation(xdean.junit.ex.param.annotation.Param.class);
          if (!isParamProviderType(anno, method.getGenericReturnType())) {
            errors.add(new Exception("Param Getter Method " + m.getName() + " should return "
                + anno.value().getParamString(getParamType()) + "."));
          }
        });
    getTestClass().getAnnotatedFields(xdean.junit.ex.param.annotation.Param.class).forEach(
        f -> {
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
          xdean.junit.ex.param.annotation.Param anno = f.getAnnotation(xdean.junit.ex.param.annotation.Param.class);
          if (!isParamProviderType(anno, field.getGenericType())) {
            errors.add(new Exception("Param Getter Field " + f.getName() + " should be "
                + anno.value().getParamString(getParamType())
                + "."));
          }
        });
  }

  protected boolean isParamProviderType(xdean.junit.ex.param.annotation.Param param, Type type) {
    if (Objects.equals(toWrapper(type), getParamType())) {
      return updateParamType(param, ParamType.VALUE);
    }
    if (type instanceof Class) {
      Class<?> clz = (Class<?>) type;
      if (clz.isArray()) {
        return Objects.equals(PrimitiveTypeUtil.toWrapper(clz.getComponentType()), getParamType())
            && updateParamType(param, ParamType.ARRAY);
      }
    }
    Type[] genericTypes = GenericUtil.getGenericTypes(type, List.class);
    return genericTypes.length == 1 && Objects.equals(genericTypes[0], getParamType())
        && updateParamType(param, ParamType.LIST);
  }

  protected boolean updateParamType(xdean.junit.ex.param.annotation.Param param, ParamType type) {
    if (param.value() == type) {
      return true;
    } else if (param.value() == ParamType.UNDEFINED) {
      AnnotationUtil.changeAnnotationValue(param, "value", type);
      return true;
    } else {
      return false;
    }
  }

  protected void initGroupBy() {
    GroupBy annotation = getTestClass().getAnnotation(GroupBy.class);
    if (annotation != null) {
      groupByParam = annotation.value() == Group.PARAM;
    }
  }

  protected void initParams() {
    params = getParamValues().stream()
        .map(param -> Param.create(getParamDisplayName(param), param))
        .collect(Collectors.toList());
  }

  protected void initDescription() {
    if (rootDescription == null) {
      rootDescription = Description.createSuiteDescription(getName(), getRunnerAnnotations());
      List<FrameworkMethod> tests = computeParamTestMethods();
      List<Param<P>> params = getParams();
      params.forEach(param -> {
        Description suite = param.getDescription();
        tests
            .forEach(test -> suite.addChild(param.getDescription(test, getTestDisplayName(test, param, groupByParam))));
        if (groupByParam) {
          rootDescription.addChild(suite);
        }
      });
      tests.forEach(test -> {
        Description suite = Description.createSuiteDescription(test.getName());
        testDescriptions.put(test, suite);
        params.forEach(
            param -> suite.addChild(param.getDescription(test, getTestDisplayName(test, param, groupByParam))));
        if (!groupByParam) {
          rootDescription.addChild(suite);
        }
      });
      Description other = getOtherDescription();
      if (!other.isEmpty()) {
        rootDescription.addChild(other);
      }
    }
  }

  private void initOther() {
    if (other == null) {
      List<FrameworkMethod> others = computeNoParamTestMethods();
      other = Description.createSuiteDescription("other");
      others.forEach(m -> other.addChild(super.describeChild(m)));
    }
  }

  /**
   * First, from subclass definition.<br>
   * Second, from method definition.
   *
   * @param errors
   * @return
   */
  protected boolean initParamClass(List<Throwable> errors) {
    Type[] genericTypes = GenericUtil.getGenericTypes(this.getClass(), ParamTestRunner.class);
    if (genericTypes.length == 0 || genericTypes[0] instanceof TypeVariable) {
      Type[] declaredTypes = computeParamTestMethods()
          .stream()
          .filter(m -> m.getMethod().getParameterCount() == 1)
          .map(m -> toWrapper(m.getMethod().getGenericParameterTypes()[0]))
          .distinct()
          .toArray(Type[]::new);
      if (declaredTypes.length != 1) {
        errors.add(new InitializationError("Con't infer the parameter type."));
        return false;
      } else {
        paramType = declaredTypes[0];
      }
    } else {
      paramType = genericTypes[0];
    }
    return true;
  }

  /******************************************************************************/

  @SuppressWarnings("unchecked")
  protected List<P> getParamValues() {
    List<Pair<ParamType, Object>> list = new ArrayList<>();
    getTestClass().getAnnotatedFields(xdean.junit.ex.param.annotation.Param.class)
        .forEach(f -> list.add(Pair.of(f.getAnnotation(xdean.junit.ex.param.annotation.Param.class).value(),
            uncheck(() -> f.get(null)))));
    getTestClass().getAnnotatedMethods(xdean.junit.ex.param.annotation.Param.class)
        .forEach(m -> list.add(Pair.of(m.getAnnotation(xdean.junit.ex.param.annotation.Param.class).value(),
            invoke(m))));
    return list.stream().flatMap(pair -> {
      ParamType type = pair.getLeft();
      Object value = pair.getRight();
      switch (type) {
      case VALUE:
        return Stream.of((P) value);
      case ARRAY:
        return Stream.of((P[]) PrimitiveTypeUtil.toWrapperArray(value));
      case LIST:
        return ((List<P>) value).stream();
      case UNDEFINED:
      default:
        throw new Error("Never happened");
      }
    }).collect(Collectors.toList());
  }

  private Object invoke(FrameworkMethod m) {
    try {
      return m.invokeExplosively(null);
    } catch (Throwable e) {
      throwAsUncheck(e);
    }
    return null;
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    return computeTestMethods();
  }

  @Override
  public Description getDescription() {
    initDescription();
    return rootDescription;
  }

  @Override
  protected Description describeChild(FrameworkMethod test) {
    if (isParamTest(test)) {
      initDescription();
      return testDescriptions.get(test);
    } else {
      return super.describeChild(test);
    }
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    if (isParamTest(method)) {
      params.forEach(param -> {
        Description description = param.getDescription(method);
        if (isIgnored(method)) {
          notifier.fireTestIgnored(description);
        } else {
          runLeaf(methodBlock(method, param), description, notifier);
        }
      });
    } else {
      super.runChild(method, notifier);
    }
  }

  @SuppressWarnings("deprecation")
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
    statement = withRules(method, test, statement);
    return statement;
  }

  protected Statement methodInvoker(FrameworkMethod method, Object test, Param<P> param) {
    return Util.statement(() -> method.invokeExplosively(test, param.getValue()));
  }

  protected void validatePublicVoidParamMethods(Class<? extends Annotation> annotation, boolean isStatic,
      List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
    for (FrameworkMethod eachTestMethod : methods) {
      eachTestMethod.validatePublicVoid(isStatic, errors);
      Method method = eachTestMethod.getMethod();
      Type[] parameterTypes = method.getGenericParameterTypes();
      if (parameterTypes.length != 1) {
        errors.add(new Exception("Method " + method.getName() + " should have only one parameter"));
      }
      // The only parameter must be the param type.
      else if (!Objects.equals(toWrapper(parameterTypes[0]), getParamType())) {
        errors.add(new Exception("Method " + method.getName() + " don't have the correct param type"));
      }
    }
  }

  @Override
  protected List<FrameworkMethod> computeTestMethods() {
    List<FrameworkMethod> list = new ArrayList<>();
    list.addAll(computeNoParamTestMethods());
    list.addAll(computeParamTestMethods());
    return list;
  }

  protected List<FrameworkMethod> computeNoParamTestMethods() {
    return getTestClass().getAnnotatedMethods(Test.class);
  }

  protected List<FrameworkMethod> computeParamTestMethods() {
    return getTestClass().getAnnotatedMethods(ParamTest.class);
  }

  protected boolean isParamTest(FrameworkMethod method) {
    return method.getAnnotation(ParamTest.class) != null;
  }

  /******************************************************************/

  protected String getTestDisplayName(FrameworkMethod method, Param<?> param, boolean groupByParam) {
    ParamTest anno = method.getAnnotation(ParamTest.class);
    return anno == null || anno.value().isEmpty() ? (groupByParam ? method.getName() : param.getName())
        : anno.value().replace("${param}", param.getName()).replace("${test}", method.getName());
  }

  protected String getParamDisplayName(P param) {
    if (param.getClass().isArray()) {
      return Arrays.deepToString((Object[]) PrimitiveTypeUtil.toWrapperArray(param));
    }
    return param.toString();
  }

  public List<Param<P>> getParams() {
    return params;
  }

  /**
   * The param type. Class or ParameterizedType
   *
   * @return
   */
  protected Type getParamType() {
    return paramType;
  }

  protected Class<?> getParamClass() {
    if (paramType instanceof Class) {
      return (Class<?>) paramType;
    } else if (paramType instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) paramType).getRawType();
    } else {
      throw new Error("Never Happen.");
    }
  }

  protected Type toWrapper(Type type) {
    if (type instanceof Class) {
      return PrimitiveTypeUtil.toWrapper((Class<?>) type);
    } else {
      return type;
    }
  }

  protected Description getOtherDescription() {
    initOther();
    return other;
  }

  @Override
  protected String getName() {
    return super.getName();
  }

  @Override
  protected Annotation[] getRunnerAnnotations() {
    return super.getRunnerAnnotations();
  }
}
