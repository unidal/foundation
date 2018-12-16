package org.unidal.agent.mixin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.agent.mixin.sample.GreetingTest;
import org.unidal.agent.mixin.sample.HttpHandlerTest;

@RunWith(Suite.class)
@SuiteClasses({

      GreetingTest.class,

      HttpHandlerTest.class,

})
public class AllTests {

}
