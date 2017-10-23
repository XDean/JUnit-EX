package xdean.junit.ex.param.demo.suite;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class Test2 {

  @Rule
  public ExpectedException ee = ExpectedException.none();

  @Ignore
  @Test
  public void other21() throws Exception {

  }

  @Test
  public void other22() throws Exception {
    ee.expect(IllegalArgumentException.class);
    throw new IllegalArgumentException();
  }
}
