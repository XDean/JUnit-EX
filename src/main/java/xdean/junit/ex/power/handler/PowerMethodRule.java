package xdean.junit.ex.power.handler;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import xdean.jex.extra.collection.Either;
import xdean.junit.ex.IStatement;

public interface PowerMethodRule extends PowerRule, MethodRule {

  @Override
  default Either<Class<? extends TestRule>, Class<? extends MethodRule>> getRuleClass() {
    return Either.right(getClass());
  }

  @Override
  default Statement apply(Statement base, FrameworkMethod method, Object target) {
    return apply(base::evaluate, method, target).toJunit();
  }

  IStatement apply(IStatement base, FrameworkMethod method, Object target);
}
