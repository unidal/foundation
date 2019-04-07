package org.unidal.agent.cat.sample.hello;

import java.io.IOException;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled(target = "org.unidal.agent.cat.sample.Hello")
public class HelloOverride {
   @CatTransaction(type = "Hello", name = "${method}")
   public HelloOverride() {
   }

   @CatTransaction(type = "Hello", name = "${method}", //
         keys = { "arg0", "return" }, values = { "${arg0}", "${return}" })
   public String bye(String name) throws IOException {
      return null;
   }

   @CatTransaction(type = "Hello", name = "${method}", //
         keys = { "arg0", "return" }, values = { "${arg0}", "${return}" })
   public String hello(String name) {
      return null;
   }
}
