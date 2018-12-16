package org.unidal.agent.mixin.sample.greeting;

import org.junit.Test;
import org.objectweb.asm.util.ASMifier;

public class DumpTest {
   private void dump(Class<?> clazz) throws Exception {
      ASMifier.main(new String[] { clazz.getName() });
   }

   @Test
   public void testGreeting() throws Exception {
      dump(Greeting.class);
   }

   @Test
   public void testGreeting2() throws Exception {
      dump(Greeting2.class);
   }

   @Test
   public void testGreetingMixin() throws Exception {
      dump(GreetingMixin.class);
   }

   @Test
   public void testGreetingMixin2() throws Exception {
      dump(GreetingMixin2.class);
   }
}
