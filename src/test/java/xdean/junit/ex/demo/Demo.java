package xdean.junit.ex.demo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.annotation.ParamTestName;

@RunWith(MyRunner.class)
public class Demo {

  static int[] ADD = { 1, 2, 2, 4, 5 };
  static int[] SQURE = { 0, 1, 4, 9, 16 };

  @Test
  @ParamTestName("$-Add")
  public void testAdd(int i) {
    assertEquals(ADD[i], i + 1);
  }

  @Test
  @ParamTestName("$-Squre")
  public void testSqure(int i) {
    assertEquals(SQURE[i], i * i);
  }
}