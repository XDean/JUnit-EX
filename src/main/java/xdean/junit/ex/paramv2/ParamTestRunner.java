package xdean.junit.ex.paramv2;

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import xdean.jex.util.reflect.ReflectUtil;

public class ParamTestRunner<P> extends BlockJUnit4ClassRunner {

  Class<P> paramClass;

  public ParamTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    super.collectInitializationErrors(errors);
    initParamClass(errors);
  }

  @SuppressWarnings("unchecked")
  protected void initParamClass(List<Throwable> errors) {
    Class<?>[] genericTypes = ReflectUtil.getGenericTypes(this.getClass(), ParamTestRunner.class);
    if (genericTypes[0] == null) {
      errors.add(new InitializationError("Children must specify T class."));
    } else {
      paramClass = (Class<P>) genericTypes[0];
    }
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    return super.getChildren();
  }

  @Override
  protected Description describeChild(FrameworkMethod method) {
    return super.describeChild(method);
  }

  @Override
  protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    super.runChild(method, notifier);
  }

  /**
   * Actually is validatePublicVoidParamMethods
   */
  @Override
  protected void validatePublicVoidNoArgMethods
      (Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);

    for (FrameworkMethod eachTestMethod : methods) {
      eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
    }
  }
}
