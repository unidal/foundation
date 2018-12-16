package org.unidal.agent.mixin.sample;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.mixin.sample.greeting.Greeting;

public class GreetingTest {
   private static Set<String> s_mixins = new LinkedHashSet<String>();

   @Test
   public void testWithMixin() throws Exception {
      System.setProperty("MIXIN_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".greeting.GreetingMixin");
      new SunJdkAttacher().loadAgent(MockAgent.class);

      Greeting greeting = new Greeting();

      Assert.assertEquals("HELLO FRANKIE", greeting.sayHello("Frankie"));
      Assert.assertEquals("bye frankie", greeting.sayBye("Frankie"));
   }

   @Test
   public void testWithMixin2() throws Exception {
      System.setProperty("MIXIN_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".greeting.GreetingMixin2");
      new SunJdkAttacher().loadAgent(MockAgent.class);

      Greeting greeting = new Greeting();

      Assert.assertEquals("sayHello: Hello Frankie", greeting.sayHello("Frankie"));
      Assert.assertEquals("sayBye: Bye Frankie", greeting.sayBye("Frankie"));
   }

   @Test
   public void testWithMixinAndMixin2() throws Exception {
      System.setProperty("MIXIN_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".greeting.GreetingMixin");
      s_mixins.add(getClass().getPackage().getName() + ".greeting.GreetingMixin2");
      new SunJdkAttacher().loadAgent(MockAgent.class);

      Greeting greeting = new Greeting();

      Assert.assertEquals("sayHello: HELLO FRANKIE", greeting.sayHello("Frankie"));
      Assert.assertEquals("sayBye: bye frankie", greeting.sayBye("Frankie"));
   }

   @Test
   public void testWithoutMixin() throws IOException {
      Greeting greeting = new Greeting();

      Assert.assertEquals("Hello Frankie", greeting.sayHello("Frankie"));
      Assert.assertEquals("Bye Frankie", greeting.sayBye("Frankie"));
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
