package xdean.junit.ex.param.demo.suite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.param.ParamTestRunner;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.Param;
import xdean.junit.ex.param.annotation.ParamTest;
import xdean.junit.ex.param.annotation.GroupBy.Group;

@RunWith(ParamTestRunner.class)
@GroupBy(Group.TEST)
public class Test1 {

  @Param
  public static final int[] PARAM = { 1, 2, 3 };

  static int[] ADD = { 1, 2, 3, 4, 5 };

  @ParamTest
  public void testAdd(int i) {
    assertEquals(ADD[i], i + 1);
  }

  @Test
  public void testOther1() {
    assertEquals(5, ADD.length);
  }
}