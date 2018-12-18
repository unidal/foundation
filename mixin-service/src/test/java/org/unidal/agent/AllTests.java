package org.unidal.agent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.agent.cat.AllCatTests;
import org.unidal.agent.mixin.AllMixinTests;

@RunWith(Suite.class)
@SuiteClasses({

      AllMixinTests.class,

      AllCatTests.class,

})
public class AllTests {

}
