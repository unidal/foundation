package org.unidal.agent.mixin.sample;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.AgentMain;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.mixin.sample.greeting.Greeting;

public class GreetingTest {
   @Test
   public void testWithMixin() throws Exception {
      System.setProperty("MIXIN_DEBUG", "false");
      System.setProperty("MIXIN_JARFILE_DEBUG", "false");
      new SunJdkAttacher().loadAgent(AgentMain.class);

      Greeting greeting = new Greeting();

      Assert.assertEquals("sayHello: HELLO FRANKIE", greeting.sayHello("Frankie"));
      Assert.assertEquals("sayBye: bye frankie", greeting.sayBye("Frankie"));
   }
}
