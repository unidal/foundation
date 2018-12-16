package org.unidal.agent.mixin.sample.greeting;

import java.io.IOException;

import org.unidal.agent.mixin.MixinMeta;

@MixinMeta(targetClass = Greeting.class)
public class GreetingMixin {
   private static boolean DEBUG = false;

   private static boolean VERBOSE = true;

   private String $_sayBye(String name) {
      return null;
   }

   private String $_sayHello(String name) {
      return null;
   }

   private void debug(String message) {
      if (DEBUG || VERBOSE) {
         System.out.println(message);
      }
   }

   public String sayBye(String name) throws IOException {
      debug("[" + getClass().getSimpleName() + "] sayBye " + name);

      return $_sayBye(name).toLowerCase();
   }

   public String sayHello(String name) {
      debug("[" + getClass().getSimpleName() + "] sayHello " + name);

      return $_sayHello(name).toUpperCase();
   }
}