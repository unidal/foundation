package org.unidal.agent.cat.sample;

import java.io.IOException;

public class Hello {
   private static boolean DEBUG = true;

   public String bye(String world) throws IOException {
      if (DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] bye: " + world);
      }

      return "Bye " + world;
   }

   public String hello(String world) {
      if (DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] hello: " + world);
      }

      return "Hello " + world;
   }
}