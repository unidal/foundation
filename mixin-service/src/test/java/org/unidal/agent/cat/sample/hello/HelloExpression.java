package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloExpression {
   @CatTransaction(type = "${class}", name = "${method}", //
         keys = { "name", "value", "return" }, values = { "${arg0}", "${arg1}", "${return}" })
   public int helloTransaction(String name, int value) {
      return value;
   }
}
