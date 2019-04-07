package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;

public class HelloOverrideTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      System.setProperty("AGENT_DEBUG", "false");

      classes.add(getClass().getPackage().getName() + ".hello.HelloOverride");
   }

   @Test
   public void test() throws Exception {
      Hello greeting = new Hello();

      greeting.hello("Frankie");
      greeting.bye("Frankie");

      expect("<init>, hello, bye");

      Thread.sleep(20);
   }
}
