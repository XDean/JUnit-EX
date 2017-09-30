package xdean.junit.ex.demo;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;

import xdean.junit.ex.paramv2.ParamTestRunner;
import xdean.junit.ex.paramv2.annotation.GroupBy;
import xdean.junit.ex.paramv2.annotation.GroupBy.Group;
import xdean.junit.ex.paramv2.annotation.Param;
import xdean.junit.ex.paramv2.annotation.ParamTest;

@RunWith(ParamTestRunner.class)
@GroupBy(Group.PARAM)
public class Demo {

  @Param
  public static final int A = 1;

  @Param
  public static List<Integer> param() {
    return Arrays.asList(1, 2, 3);
  }

  static int[] ADD = { 1, 2, 2, 4, 5 };
  static int[] SQURE = { 0, 1, 4, 9, 16 };

  @ParamTest("$-Add")
  public void testAdd(int i) {
    assertEquals(ADD[i], i + 1);
  }

  @ParamTest("$-Squre")
  public void testSqure(int i) {
    assertEquals(SQURE[i], i * i);
  }
}