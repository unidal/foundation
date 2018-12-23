package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloService {
   @CatTransaction(type = "Hello", name = "void helloDouble(double value)", keys = "arg0", values = "${arg0}")
   public void helloDouble(double value) {
   }
}
