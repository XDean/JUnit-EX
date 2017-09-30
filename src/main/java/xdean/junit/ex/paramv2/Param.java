package xdean.junit.ex.paramv2;

import static xdean.jex.util.cache.CacheUtil.cache;

import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

import xdean.junit.ex.annotation.ParamTestName;

public class Param<P> {

  String name;
  P value;

  public Description getDescription(FrameworkMethod method) {
    return cache(this, method, () -> Description.createTestDescription(name, getName(method).replace("$", name)));
  }

  private String getName(FrameworkMethod method) {
    ParamTestName nameAnno = method.getAnnotation(ParamTestName.class);
    return nameAnno == null ? method.getName() : nameAnno.value();
  }
}
