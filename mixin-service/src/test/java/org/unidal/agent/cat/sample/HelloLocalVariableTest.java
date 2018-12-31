package org.unidal.agent.cat.sample;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;
import org.objectweb.asm.util.ASMifier;
import org.unidal.agent.cat.sample.hello.HelloLocalVariable;

public class HelloLocalVariableTest extends AbstractHelloTest {
   public static void main(String[] args) throws IOException {
      ASMifier.main(new String[] { HelloLocalVariable.class.getName() });
   }

   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloLocalVariable");
   }

   @Test
   public void test() throws Exception {
      invokeAllMethods(new HelloLocalVariable());

      expect("helloEvent, helloTransaction");

      Thread.sleep(20);
   }
}
