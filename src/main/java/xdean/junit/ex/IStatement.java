package xdean.junit.ex;

public interface IStatement {
  void evaluate() throws Throwable;

  default org.junit.runners.model.Statement toJunit() {
    return new org.junit.runners.model.Statement() {
      @Override
      public void evaluate() throws Throwable {
        IStatement.this.evaluate();
      }
    };
  }
}