package org.unidal.mixin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.mixin.sample.GreetingTest;
import org.unidal.mixin.sample.HttpHandlerTest;

@RunWith(Suite.class)
@SuiteClasses({

      GreetingTest.class,

      HttpHandlerTest.class,

})
public class AllTests {

}
