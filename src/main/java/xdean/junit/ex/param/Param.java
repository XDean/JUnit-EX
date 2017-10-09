package xdean.junit.ex.param;

import static xdean.jex.util.cache.CacheUtil.cache;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

import xdean.jex.util.cache.CacheUtil;

@Getter
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class Param<P> {

  public static <P> Param<P> create(String name, P value) {
    return new Param<P>(name, value);
  }

  String name;
  P value;

  public Description getDescription() {
    return cache(this, Description.class, () -> Description.createSuiteDescription(getName()));
  }

  public Description getDescription(FrameworkMethod method, String displayName) {
    return cache(this, method, () -> Description.createTestDescription(
        String.format("Param: %s, Test: %s", name, method.getName()), displayName));
  }

  public Description getDescription(FrameworkMethod method) {
    return CacheUtil.<Description> get(this, method).get();
  }
}
