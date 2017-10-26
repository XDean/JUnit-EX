package xdean.junit.ex.power.handler;

import org.junit.runner.Description;

import xdean.junit.ex.power.PowerUpHandler;
import xdean.junit.ex.power.PowerUpResult;

public interface SimpleBefore extends PowerUpHandler {
  @Override
  default PowerUpResult powerup(Class<?> testClass) {
    NamedAction action = getAction();
    return PowerUpResult.justBefore(
        Description.createTestDescription(getClass(), action.getName()),
        action.getAction());
  }

  NamedAction getAction();
}