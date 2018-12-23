package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloMethod {
   private static final String TYPE = "HelloMethod";

   @CatTransaction(type = TYPE, name = "${method}", keys = { "class", "arg0" }, values = { "${class}", "${arg0}" })
   public HelloMethod(String str) {
   }

   @CatTransaction(type = TYPE, name = "${method}", keys = { "class", "arg0" }, values = { "${class}", "${arg0}" })
   public void helloNormal(String str) {
   }

   @CatTransaction(type = TYPE, name = "${method}", keys = { "class", "arg0" }, values = { "${class}", "${arg0}" })
   public static void helloStatic(String str) {
   }
}
