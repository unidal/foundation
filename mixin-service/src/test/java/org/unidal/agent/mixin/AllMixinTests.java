package org.unidal.agent.mixin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.agent.mixin.asm.ClassGeneratorTest;
import org.unidal.agent.mixin.sample.GreetingTest;

@RunWith(Suite.class)
@SuiteClasses({

      MixinModelTest.class,

      MixinModelBuilderTest.class,

      MixinModelAggregatorTest.class,

      ClassGeneratorTest.class,

      GreetingTest.class,

})
public class AllMixinTests {

}
