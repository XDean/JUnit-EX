package xdean.junit.ex.demo;

import java.util.Arrays;
import java.util.List;

import org.junit.runners.model.InitializationError;

import xdean.junit.ex.paramv2.ParamTestRunner;

public class MyRunner extends ParamTestRunner<Integer> {
  public MyRunner(Class<?> clz) throws InitializationError {
    super(clz);
  }

  @Override
  protected List<Integer> getParamValues() {
    return Arrays.asList(0, 1, 2, 3, 4);
  }
}