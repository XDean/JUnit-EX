package xdean.junit.ex.power.demo.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import xdean.junit.ex.power.annotation.PowerUp;
import xdean.junit.ex.power.handler.NamedAction;
import xdean.junit.ex.power.handler.SimpleBefore;

public class HelloWorldFirstHandler implements SimpleBefore {

  @Target(TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @PowerUp(HelloWorldFirstHandler.class)
  public @interface HelloWorldFirst {

  }

  @Override
  public NamedAction getAction() {
    return NamedAction.create("HelloWorld", this::HelloWorld);
  }

  private void HelloWorld() {
    System.out.println("Hello World");
  }

}
