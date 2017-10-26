package xdean.junit.ex.power;

@FunctionalInterface
public interface PowerUpHandler {
  PowerUpResult powerup(Class<?> testClass);
}
