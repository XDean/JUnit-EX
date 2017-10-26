package xdean.junit.ex.power;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;

import xdean.jex.extra.Pair;

public class PowerUpResult {
  public static PowerUpResult justBefore(Description desc, Runnable run) {
    return new PowerUpResult().addBefore(desc, run);
  }

  public static PowerUpResult justAfter(Description desc, Runnable run) {
    return new PowerUpResult().addBefore(desc, run);
  }

  public static PowerUpResult justClass(Class<?> clz) {
    return new PowerUpResult().setNewTestClass(clz);
  }

  private List<Pair<Description, Runnable>> before = new ArrayList<>();
  private List<Pair<Description, Runnable>> after = new ArrayList<>();
  private Class<?> actualClass;

  public PowerUpResult addBefore(Description desc, Runnable run) {
    before.add(Pair.of(desc, run));
    return this;
  }

  public PowerUpResult addAfter(Description desc, Runnable run) {
    after.add(Pair.of(desc, run));
    return this;
  }

  public List<Pair<Description, Runnable>> getBefore() {
    return before;
  }

  public List<Pair<Description, Runnable>> getAfter() {
    return after;
  }

  public PowerUpResult setNewTestClass(Class<?> newTestClass) {
    this.actualClass = newTestClass;
    return this;
  }

  public Class<?> getActualClass() {
    return actualClass;
  }

  public void mergeAfter(PowerUpResult other) {
    getBefore().addAll(other.getBefore());
    getAfter().addAll(0, other.getAfter());
    Class<?> newClass = other.getActualClass();
    actualClass = newClass == null ? actualClass : newClass;
  }
}
