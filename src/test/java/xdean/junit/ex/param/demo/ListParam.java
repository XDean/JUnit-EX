package xdean.junit.ex.param.demo;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.param.ParamTestRunner;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.GroupBy.Group;
import xdean.junit.ex.param.annotation.Param;
import xdean.junit.ex.param.annotation.ParamTest;

@RunWith(ParamTestRunner.class)
@GroupBy(Group.TEST)
public class ListParam {

  @Param
  public static final List<Integer> PARAM = Arrays.asList(10, 1);
  @Param
  public static final List<Integer> PARAM_ARRAY = Arrays.asList(10, 1);

  @ParamTest
  public void testLength(List<Integer> i) {
    assertEquals(2, i.size());
  }

  @ParamTest
  public void testFirst(List<Integer> i) {
    assertEquals(10, i.get(0).intValue());
  }

  @Test(expected = RuntimeException.class)
  public void testError() {
    throw new RuntimeException();
  }
}