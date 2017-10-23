package xdean.junit.ex.param.demo.suite;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.param.ParamTestRunner;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.Param;
import xdean.junit.ex.param.annotation.ParamTest;
import xdean.junit.ex.param.annotation.GroupBy.Group;

@RunWith(ParamTestRunner.class)
@GroupBy(Group.PARAM)
public class Test1 {

  @Param
  public static final int[] PARAM = { 0, 1, 3, 4 };

  static int[] ADD = { 1, 2, 3, 4, 5 };
  static int[] ADD_100 = { 100, 101, 102, 103, 104 };

  @ParamTest
  public void testAdd(int i) {
    assertEquals(ADD[i], i + 1);
  }

  @Ignore
  @ParamTest
  public void testAdd100(int i) {
    fail();
    assertEquals(ADD_100[i], i + 100);
  }

  @Test
  public void other1() {
    assertEquals(5, ADD.length);
  }
}