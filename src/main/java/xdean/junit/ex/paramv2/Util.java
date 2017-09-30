package xdean.junit.ex.paramv2;

import org.junit.runners.model.Statement;

public interface Util {

  static Statement statement(xdean.junit.ex.paramv2.IStatement s) {
    return s.toJunit();
  }

  @SuppressWarnings("unchecked")
  static <T> T reflectCall(IReflectiveCallable c) throws Throwable {
    return (T) c.run();
  }
}
