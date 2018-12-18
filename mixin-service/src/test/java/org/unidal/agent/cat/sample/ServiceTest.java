package org.unidal.agent.cat.sample;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.util.ASMifier;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.cat.sample.hello.HelloService;
import org.unidal.agent.cat.sample.hello.HelloService2;

public class ServiceTest {
   private static Set<String> s_mixins = new LinkedHashSet<String>();

   public static void main(String[] args) throws Exception {
      ASMifier.main(new String[] { HelloService2.class.getName() });
   }

   @Test
   public void testHelloService() throws Exception {
      System.setProperty("MIXIN_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloService");
      new SunJdkAttacher().loadAgent(MockAgent.class);
      HelloService service = new HelloService();

      service.hello("Frankie");
      //Assert.assertEquals("Hello Frankie", service.hello("Frankie"));
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
