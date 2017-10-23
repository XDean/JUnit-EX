package xdean.junit.ex.param.demo.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import xdean.junit.ex.param.ParamSuite;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.GroupBy.Group;

@RunWith(ParamSuite.class)
@SuiteClasses({ Test1.class, Test2.class })
@GroupBy(Group.TEST)
public class SmallSuite {

}
