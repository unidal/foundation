package org.unidal.agent.cat.sample;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.mixin.sample.greeting.Greeting;

public class HelloOverrideTest {
   private static Set<String> s_mixins = new LinkedHashSet<String>();

   @Before
   public void before() throws Exception {
      System.setProperty("CAT_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloOverride");
      new SunJdkAttacher().loadAgent(MockAgent.class);
   }

   @Test
   public void test() throws Exception {
      Greeting greeting = new Greeting();

      greeting.sayHello("Frankie");
      greeting.sayBye("Frankie");

      Thread.sleep(1);
   }

   public static class MockAgent {
      public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
         ClassTransformer transformer = new ClassTransformer(inst);

         for (String mixin : s_mixins) {
            transformer.register(mixin);
         }

         inst.addTransformer(transformer, false);
      }
   }
}
