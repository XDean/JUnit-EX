package xdean.junit.ex.param;

import static xdean.jex.util.cache.CacheUtil.cache;
import lombok.Value;

import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;

import xdean.junit.ex.ParamTest;
import xdean.junit.ex.annotation.ParamTestName;

@Value
class ParamMethodTest<P> {
  FrameworkMethod method;
  ParamTest<P> actualTest;

  public void run(Param<P> param) throws Throwable {
    actualTest.run(param.getValue());
  }

  public Description getDescription(Param<P> p) {
    return cache(this, p,
        () -> Description.createTestDescription(p.getName(), getName().replace("$", p.getName())));
  }

  private String getName() {
    ParamTestName nameAnno = method.getAnnotation(ParamTestName.class);
    return nameAnno == null ? method.getName() : nameAnno.value();
  }
}