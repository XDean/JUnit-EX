package xdean.junit.ex.param.demo;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.param.ParamTestRunner;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.GroupBy.Group;
import xdean.junit.ex.param.annotation.Param;
import xdean.junit.ex.param.annotation.ParamTest;

@RunWith(ParamTestRunner.class)
@GroupBy(Group.PARAM)
public class ArrayParam {

  @Param
  public static final int[] PARAM = { 10, 1 };
  @Param
  public static final int[][] PARAM_ARRAY = { { 10, 2 }, { 10, 3 } };

  @ParamTest
  public void testLength(int[] i) {
    assertEquals(2, i.length);
  }

  @ParamTest
  public void testFirst(int[] i) {
    assertEquals(10, i[0]);
  }

  @Test(expected = RuntimeException.class)
  public void testError() {
    throw new RuntimeException();
  }
}