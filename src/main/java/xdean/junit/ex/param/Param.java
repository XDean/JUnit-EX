package xdean.junit.ex.param;

import lombok.Value;

@Value
public class Param<P> {
  String name;
  P value;
}
