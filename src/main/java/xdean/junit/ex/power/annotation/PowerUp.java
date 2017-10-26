package xdean.junit.ex.power.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import xdean.junit.ex.power.PowerUpHandler;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface PowerUp {
  Class<? extends PowerUpHandler> value();
}
