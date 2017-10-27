package xdean.junit.ex.power.demo.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runners.model.FrameworkMethod;

import xdean.junit.ex.IStatement;
import xdean.junit.ex.power.annotation.PowerUp;
import xdean.junit.ex.power.handler.PowerMethodRule;

public class SayHelloHandler implements PowerMethodRule {

  @Target(TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @PowerUp(SayHelloHandler.class)
  public @interface SayHello {

  }

  @Override
  public IStatement apply(IStatement base, FrameworkMethod method, Object target) {
    return () -> {
      System.out.println("Hello " + method);
      base.evaluate();
    };
  }
}
