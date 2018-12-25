package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.sample.hello.HelloException;

public class HelloExceptionTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloException");
   }

   @Test
   public void test() throws Exception {
      invokeAllMethods(new HelloException());

      expect("void helloError(), void helloException(), void helloIOException(), "
            + "void helloRuntimeException(), void helloThrowable()");

      Thread.sleep(20);
   }
}
