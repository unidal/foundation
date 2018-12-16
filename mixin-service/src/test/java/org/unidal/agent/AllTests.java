package org.unidal.agent;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.agent.mixin.asm.ClassGeneratorTest;
import org.unidal.agent.mixin.asm.MixinModelAggregatorTest;
import org.unidal.agent.mixin.asm.MixinModelBuilderTest;
import org.unidal.agent.mixin.asm.MixinModelTest;
import org.unidal.agent.mixin.sample.GreetingTest;

@RunWith(Suite.class)
@SuiteClasses({

      MixinModelTest.class,

      MixinModelBuilderTest.class,

      MixinModelAggregatorTest.class,

      ClassGeneratorTest.class,

      GreetingTest.class,

})
public class AllTests {

}
