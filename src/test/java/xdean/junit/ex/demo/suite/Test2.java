package xdean.junit.ex.demo.suite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.param.ParamTestRunner;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.Param;
import xdean.junit.ex.param.annotation.ParamTest;
import xdean.junit.ex.param.annotation.GroupBy.Group;

@RunWith(ParamTestRunner.class)
@GroupBy(Group.PARAM)
public class Test2 {

  @Param
  public static final int[] PARAM = { 1, 2, 3 };

  static int[] SQURE = { 0, 1, 4, 9, 16 };

  @ParamTest
  public void testSqure(int i) {
    assertEquals(SQURE[i], i * i);
  }

  @Test
  public void testOther2() {
    assertEquals(5, SQURE.length);
  }
}