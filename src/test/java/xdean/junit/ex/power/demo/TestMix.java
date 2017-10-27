package xdean.junit.ex.power.demo;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.power.PowerRunner;
import xdean.junit.ex.power.annotation.PowerUp;
import xdean.junit.ex.power.demo.annotation.HelloWorldFirstHandler.HelloWorldFirst;
import xdean.junit.ex.power.demo.annotation.SayGoodByeHandler.SayGoodBye;
import xdean.junit.ex.power.demo.annotation.SayHelloHandler;

@PowerUp(SayHelloHandler.class)
@HelloWorldFirst
@SayGoodBye
@RunWith(PowerRunner.class)
public class TestMix {

  @Test
  public void test1() throws Exception {
    System.out.println("TestMix.test1()");
  }

  @Test
  public void test2() {
    System.out.println("TestMix.test2()");
  }
}
