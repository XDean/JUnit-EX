package xdean.junit.ex.power.demo;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.power.PowerRunner;
import xdean.junit.ex.power.annotation.PowerUp;
import xdean.junit.ex.power.demo.annotation.HelloWorldFirstHandler;
import xdean.junit.ex.power.demo.annotation.SayGoodByeHandler;
import xdean.junit.ex.power.demo.annotation.SayHelloHandler;

@PowerUp(SayGoodByeHandler.class)
@PowerUp(SayHelloHandler.class)
@PowerUp(HelloWorldFirstHandler.class)
@RunWith(PowerRunner.class)
public class TestPowerUp {

  @Test
  public void test1() throws Exception {
    System.out.println("TestPowerUp.test1()");
  }

  @Test
  public void test2() {
    System.out.println("TestPowerUp.test2()");
  }
}
