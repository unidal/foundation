package org.unidal.mixin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unidal.mixin.asm.ClassGeneratorTest;
import org.unidal.mixin.asm.MixinModelAggregatorTest;
import org.unidal.mixin.asm.MixinModelBuilderTest;
import org.unidal.mixin.asm.MixinModelTest;
import org.unidal.mixin.sample.GreetingTest;

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
