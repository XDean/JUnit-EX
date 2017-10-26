package xdean.junit.ex.power.demo.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runner.Description;

import xdean.junit.ex.power.PowerUp;
import xdean.junit.ex.power.PowerUpResult;
import xdean.junit.ex.power.annotation.PowerUpHandler;

public class HelloWorldFirstHandler implements PowerUp {

  @PowerUpHandler(HelloWorldFirstHandler.class)
  @Target(TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface HelloWorldFirst {

  }

  @Override
  public PowerUpResult powerup(Class<?> testClass) {
    return PowerUpResult.justBefore(Description.createTestDescription(HelloWorldFirstHandler.class, "Hello World"),
        null);
  }

}
