package xdean.junit.ex.param;

import org.junit.runners.model.Statement;

interface Util {

  static Statement statement(xdean.junit.ex.param.IStatement s) {
    return s.toJunit();
  }

  @SuppressWarnings("unchecked")
  static <T> T reflectCall(IReflectiveCallable c) throws Throwable {
    return (T) c.run();
  }
}
