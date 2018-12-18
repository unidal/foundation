package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatEvent;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloService {
   @CatTransaction(type = "Service", name = "hello", status = "${return}:200,201", //
         keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   @CatEvent(type = "Guest", name = "${arg0}")
   public String hello(String name) {
       return "Hello " + name;
   }
}
