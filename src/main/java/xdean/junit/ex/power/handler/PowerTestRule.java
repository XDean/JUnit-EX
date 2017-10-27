package xdean.junit.ex.power.handler;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import xdean.jex.extra.Either;
import xdean.junit.ex.IStatement;

public interface PowerTestRule extends PowerRule, TestRule {
  @Override
  default Either<TestRule, MethodRule> getRule() {
    return Either.left(this);
  }

  @Override
  default Statement apply(Statement base, Description description) {
    return apply(base::evaluate, description).toJunit();
  }

  IStatement apply(IStatement base, Description description);
}
