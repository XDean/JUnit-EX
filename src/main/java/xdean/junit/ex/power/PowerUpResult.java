package xdean.junit.ex.power;

import java.util.Map;

import org.apache.tools.ant.types.Description;

public class PowerUpResult {
  private Map<Description, Runnable> before;
  private Map<Description, Runnable> after;
  private Class<?> newTestClass;

  public PowerUpResult addBefore(Description desc, Runnable run) {
    before.put(desc, run);
    return this;
  }

  public PowerUpResult addAfter(Description desc, Runnable run) {
    after.put(desc, run);
    return this;
  }

  public Map<Description, Runnable> getBefore() {
    return before;
  }

  public Map<Description, Runnable> getAfter() {
    return after;
  }

  public void setNewTestClass(Class<?> newTestClass) {
    this.newTestClass = newTestClass;
  }

  public Class<?> getNewTestClass() {
    return newTestClass;
  }
}
