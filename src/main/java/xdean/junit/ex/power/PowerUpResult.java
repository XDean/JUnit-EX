package xdean.junit.ex.power;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;

import io.reactivex.functions.Action;
import xdean.jex.extra.collection.Pair;

public class PowerUpResult {
  private static final PowerUpResult EMPTY = new PowerUpResult();

  public static PowerUpResult justBefore(Description desc, Action run) {
    return new PowerUpResult().addBefore(desc, run);
  }

  public static PowerUpResult justAfter(Description desc, Action run) {
    return new PowerUpResult().addBefore(desc, run);
  }

  public static PowerUpResult justClass(Class<?> clz) {
    return new PowerUpResult().setNewTestClass(clz);
  }

  public static PowerUpResult create() {
    return new PowerUpResult();
  }

  public static PowerUpResult empty() {
    return EMPTY;
  }

  private List<Pair<Description, Action>> before = new ArrayList<>();
  private List<Pair<Description, Action>> after = new ArrayList<>();
  private Class<?> actualClass;

  private PowerUpResult() {

  }

  public PowerUpResult addBefore(Description desc, Action run) {
    before.add(Pair.of(desc, run));
    return this;
  }

  public PowerUpResult addAfter(Description desc, Action run) {
    after.add(Pair.of(desc, run));
    return this;
  }

  public List<Pair<Description, Action>> getBefore() {
    return before;
  }

  public List<Pair<Description, Action>> getAfter() {
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
