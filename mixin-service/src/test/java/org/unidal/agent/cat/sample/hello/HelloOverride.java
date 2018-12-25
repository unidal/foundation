package org.unidal.agent.cat.sample.hello;

import java.io.IOException;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled(target = "org.unidal.agent.cat.sample.greeting.Greeting")
public class HelloOverride {
   @CatTransaction(type = "Greeting", name = "${method}")
   public HelloOverride() {
   }

   @CatTransaction(type = "Greeting", name = "${method}", //
         keys = { "arg0", "return" }, values = { "${arg0}", "${return}" })
   public String sayBye(String name) throws IOException {
      return null;
   }

   @CatTransaction(type = "Greeting", name = "${method}", //
         keys = { "arg0", "return" }, values = { "${arg0}", "${return}" })
   public String sayHello(String name) {
      return null;
   }
}
