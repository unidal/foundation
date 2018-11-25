package org.unidal.mixin.sample.greeting;

import java.io.IOException;

public class Greeting {
   private static boolean DEBUG = true;

   public String sayBye(String name) throws IOException {
      if (DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] sayBye: " + name);
      }

      return "Bye " + name;
   }

   public String sayHello(String name) {
      if (DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] sayHello: " + name);
      }

      return "Hello " + name;
   }
}