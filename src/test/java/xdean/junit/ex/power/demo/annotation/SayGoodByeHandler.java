package xdean.junit.ex.power.demo.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runner.Description;

import xdean.junit.ex.IStatement;
import xdean.junit.ex.power.annotation.PowerUp;
import xdean.junit.ex.power.handler.PowerTestRule;

public class SayGoodByeHandler implements PowerTestRule {

  @Target(TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @PowerUp(SayGoodByeHandler.class)
  public @interface SayGoodBye {

  }

  @Override
  public IStatement apply(IStatement base, Description description) {
    return () -> {
      base.evaluate();
      System.out.println("GoodBye " + description.getDisplayName());
    };
  }
}
