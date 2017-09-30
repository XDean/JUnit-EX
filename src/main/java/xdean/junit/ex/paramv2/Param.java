package xdean.junit.ex.paramv2;

import static xdean.jex.util.cache.CacheUtil.*;
import lombok.Value;

import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

import xdean.jex.util.cache.CacheUtil;

@Value
class Param<P> {

  String name;
  P value;

  public Description getDescription(FrameworkMethod method, String displayName) {
    return cache(this, method, () -> Description.createTestDescription(name, displayName));
  }

  public Description getDescription(FrameworkMethod method) {
    return CacheUtil.<Description> get(this, method).get();
  }
}
