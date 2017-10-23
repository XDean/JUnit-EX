package xdean.junit.ex.param.demo.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import xdean.junit.ex.param.ParamSuite;
import xdean.junit.ex.param.annotation.GroupBy;
import xdean.junit.ex.param.annotation.GroupBy.Group;

@RunWith(ParamSuite.class)
@SuiteClasses({ SmallSuite.class, Test3.class })
@GroupBy(Group.PARAM)
public class BigSuite {

}
