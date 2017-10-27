package xdean.junit.ex.power.demo;

import org.junit.Test;
import org.junit.runner.RunWith;

import xdean.junit.ex.power.PowerRunner;
import xdean.junit.ex.power.demo.annotation.HelloWorldFirstHandler.HelloWorldFirst;
import xdean.junit.ex.power.demo.annotation.SayGoodByeHandler.SayGoodBye;
import xdean.junit.ex.power.demo.annotation.SayHelloHandler.SayHello;

@HelloWorldFirst
@SayGoodBye
@SayHello
@RunWith(PowerRunner.class)
public class TestAnnotationDirect {

  @Test
  public void test1() throws Exception {
    System.out.println("TestAnnotationDirect.test1()");
  }

  @Test
  public void test2() {
    System.out.println("TestAnnotationDirect.test2()");
  }
}
