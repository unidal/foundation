package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.sample.hello.HelloReturnType;

public class HelloReturnTypeTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloReturnType");
   }

   @Test
   public void test() throws Exception {
      invokeAllMethods(new HelloReturnType());

      expect("boolean helloBoolean(), byte helloByte(), char helloChar(), double helloDouble(), float helloFloat(), "
            + "int helloInt(), String helloString(), long helloLong(), short helloShort(), void helloVoid()");

      Thread.sleep(20);
   }
}
