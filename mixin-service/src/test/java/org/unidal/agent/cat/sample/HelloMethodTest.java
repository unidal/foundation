package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.sample.hello.HelloMethod;

public class HelloMethodTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloMethod");
   }

   @Test
   public void test() throws Exception {
      invokeAllMethods(new HelloMethod());

      expect("<init>, helloStatic, helloNormal");

      Thread.sleep(20);
   }
}
