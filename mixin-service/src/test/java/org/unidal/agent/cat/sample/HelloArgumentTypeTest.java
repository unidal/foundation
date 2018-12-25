package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.sample.hello.HelloArgumentType;

public class HelloArgumentTypeTest extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloArgumentType");
   }

   @Test
   public void test() throws Exception {
      invokeAllMethods(new HelloArgumentType());

      expect("void helloBoolean(boolean value), void helloByte(byte value), void helloChar(char value), "
            + "void helloDouble(double value), void helloFloat(float value), void helloInt(int value), "
            + "void helloLong(long value), void helloShort(short value), void helloString(String value)");

      Thread.sleep(20);
   }
}
