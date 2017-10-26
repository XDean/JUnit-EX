package xdean.junit.ex.power.demo;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.power.PowerRunner;
import xdean.junit.ex.power.demo.annotation.HelloWorldFirstHandler.HelloWorldFirst;

@HelloWorldFirst
@RunWith(PowerRunner.class)
public class PowerUpTest {
  @Test
  public void test1() throws Exception {
    System.out.println("tested");
  }

  @Test
  public void test2() {

  }
}
