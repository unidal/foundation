package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.sample.hello.HelloAnnotation;

public class HelloAnnotationTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloAnnotation");
   }

   @Test
   public void test() throws Exception {
      invokeAllMethods(new HelloAnnotation());

      expect("helloEventData, helloTransactionData, helloTransactionEventData, helloEvent, "
            + "helloTransaction, helloTransactionEvent");

      Thread.sleep(20);
   }
}
