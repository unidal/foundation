package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatEvent;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloLocalVariable {
   private static final String TYPE = "HelloLocalVariable";

   @CatTransaction(type = TYPE, name = "${method}", keys = { "arg0", "return" }, values = { "${arg0}", "${return}" })
   public int helloTransaction(String str) {
      int a = 1;
      int b = 2;
      int c = a * b + a + b + 1;
      int d = c * c;

      return d;
   }

   @CatEvent(type = TYPE, name = "${method}", keys = { "arg0", "return" }, values = { "${arg0}", "${return}" })
   public int helloEvent(String str) {
      int a = 1;
      int b = 2;
      int c = a * b + a + b + 1;
      int d = c * c;

      return d;
   }
}
