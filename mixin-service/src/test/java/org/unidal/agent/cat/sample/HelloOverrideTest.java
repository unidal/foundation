package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.sample.greeting.Greeting;

public class HelloOverrideTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      System.setProperty("CAT_DEBUG", "true");

      classes.add(getClass().getPackage().getName() + ".hello.HelloOverride");
   }

   @Test
   public void test() throws Exception {
      Greeting greeting = new Greeting();

      greeting.sayHello("Frankie");
      greeting.sayBye("Frankie");

      expect("<init>, sayHello, sayBye");

      Thread.sleep(20);
   }
}
