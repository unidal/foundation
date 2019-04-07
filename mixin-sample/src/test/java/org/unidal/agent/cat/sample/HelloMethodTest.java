package org.unidal.agent.cat.sample;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.AgentMain;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.mixin.sample.greeting.Greeting;

public class HelloMethodTest  {
   @Test
   public void testWithMixin() throws Exception {
      System.setProperty("AGENT_DEBUG", "true");
      new SunJdkAttacher().loadAgent(AgentMain.class);

      Greeting greeting = new Greeting();

      Assert.assertEquals("sayHello: HELLO FRANKIE", greeting.sayHello("Frankie"));
      Assert.assertEquals("sayBye: bye frankie", greeting.sayBye("Frankie"));
   }
}
