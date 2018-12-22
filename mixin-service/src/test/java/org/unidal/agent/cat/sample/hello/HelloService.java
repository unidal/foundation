package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloService {

   @CatTransaction(type = "Hello", name = "void hello()")
   public void hello() throws RuntimeException {
      throw new IllegalArgumentException();
   }

   // @CatTransaction(type = "Service", name = "hello2", //
   // keys = { "name", "value" }, values = { "${arg0}", "${arg1}" })
   // @CatEvent(type = "Guest", name = "${return}")
   // public String hello(String name, int value) {
   // return "Haha " + name;
   // }

   // @CatTransaction(type = "Service", name = "void hello(String name)", //
   // keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   // public void hello11(String name) throws IOException, IllegalStateException {
   // System.out.println("Hello " + name);
   // }
   //
   // @CatTransaction(type = "Service", name = "hello12", //
   // keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   // @CatEvent(type = "Guest", name = "${arg0}")
   // public void hello12(String name) {
   // System.out.println("Hello " + name);
   // }
   //
   // @CatEvent(type = "Guest", name = "${arg0}")
   // public void hello13(String name) {
   // System.out.println("Hello " + name);
   // }

   // @CatTransaction(type = "Service", name = "hello11", //
   // keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   // public String hello21(String name) {
   // return "Hello " + name;
   // }
   //
   // @CatTransaction(type = "Service", name = "hello22", //
   // keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   // @CatEvent(type = "Guest", name = "${arg0}")
   // public String hello22(String name) {
   // return "Hello " + name;
   // }
   //
   // @CatEvent(type = "Guest", name = "${arg0}")
   // public String hello23(String name) {
   // return "Hello " + name;
   // }
}
