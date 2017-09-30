package xdean.junit.ex;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface ParamTest<P> {

  void run(P param) throws Throwable;

  @FunctionalInterface
  public interface ReflectParamTest<P> extends ParamTest<P> {
    @Override
    default void run(P param) throws Throwable {
      try {
        runReflectiveCall(param);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    void runReflectiveCall(P param) throws Throwable;
  }
}
