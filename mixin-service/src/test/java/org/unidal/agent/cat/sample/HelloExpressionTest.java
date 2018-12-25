package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.sample.hello.HelloExpression;

public class HelloExpressionTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloExpression");
   }

   @Test
   public void test() throws Exception {
      invokeAllMethods(new HelloExpression());

      expect("helloArguments, helloMethod");

      Thread.sleep(20);
   }
}
