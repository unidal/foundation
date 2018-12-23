package org.unidal.agent.cat.sample;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;

public class AllHelloTests {
   private static Set<String> s_mixins = new LinkedHashSet<String>();

   @Before
   public void before() throws Exception {
      System.setProperty("CAT_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloAnnotation");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloArgumentType");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloException");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloExpression");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloMethod");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloReturnType");

      new SunJdkAttacher().loadAgent(MockAgent.class);
   }

   @Test
   public void test() throws Exception {
      new HelloAnnotationTest().test();
      new HelloArgumentTypeTest().test();
      new HelloExceptionTest().test();
      new HelloExpressionTest().test();
      new HelloMethodTest().test();
      new HelloReturnTypeTest().test();
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
