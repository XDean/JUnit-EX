package xdean.junit.ex.param;

import java.lang.reflect.InvocationTargetException;

public interface IReflectiveCallable {
  default Object run() throws Throwable {
    try {
      return runReflectiveCall();
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  Object runReflectiveCall() throws Throwable;
}