package xdean.junit.ex.param;

import static xdean.jex.util.cache.CacheUtil.cache;

import java.util.Objects;

import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

import com.google.common.base.MoreObjects;

import xdean.jex.util.cache.CacheUtil;

class Param<P> {

  public static <P> Param<P> create(String name, P value) {
    return new Param<>(name, value);
  }

  private final String name;
  private final P value;

  private Param(String name, P value) {
    this.name = name;
    this.value = value;
  }

  public Description getDescription() {
    return cache(this, Description.class, () -> Description.createSuiteDescription(getName()));
  }

  public Description getDescription(FrameworkMethod method, String displayName) {
    return cache(this, method, () -> Description.createTestDescription(
        method.getMethod().getDeclaringClass().getName(), displayName,
        String.format("Param: %s, Test: %s", name, method.getName())));
  }

  public Description getDescription(FrameworkMethod method) {
    return CacheUtil.<Description> get(this, method).get();
  }

  public String getName() {
    return name;
  }

  public P getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (obj instanceof Param) {
      return false;
    }
    Param<?> other = (Param<?>) obj;
    return Objects.equals(name, other.name) && Objects.equals(value, other.value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("value", value).toString();
  }
}
