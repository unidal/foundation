package org.unidal.agent.mixin.sample.greeting;

import java.io.IOException;

public class Greeting2 {
   private static Helper m_helper = new Helper();

   private static boolean DEBUG = false;

   private static boolean VERBOSE = true;

   private static boolean $_DEBUG = true;

   public String sayBye(String name) throws IOException {
      m_helper.begin("sayBye:", name);

      try {
         return "sayBye: " + $_sayBye(name);
      } finally {
         m_helper.end();
      }
   }

   public String sayHello(String name) {
      m_helper.begin("sayHello:", name);

      try {
         return "sayHello: " + $_sayHello(name);
      } finally {
         m_helper.end();
      }
   }

   private void debug(String message) {
      if (DEBUG || VERBOSE) {
         System.out.println(message);
      }
   }

   private String $_sayBye(String name) throws IOException {
      debug("[" + getClass().getSimpleName() + "] sayBye " + name);

      return $2_sayBye(name).toLowerCase();
   }

   private String $_sayHello(String name) {
      debug("[" + getClass().getSimpleName() + "] sayHello " + name);

      return $2_sayHello(name).toUpperCase();
   }

   private String $2_sayBye(String name) throws IOException {
      if ($_DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] sayBye: " + name);
      }

      return "Bye " + name;
   }

   private String $2_sayHello(String name) {
      if ($_DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] sayHello: " + name);
      }

      return "Hello " + name;
   }

   private static class Helper {
      private String m_method;

      private String m_name;

      public void begin(String method, String name) {
         m_method = method;
         m_name = name;

         System.out.println(String.format("BEGIN: %s(%s)", m_method, m_name));
      }

      public void end() {
         System.out.println(String.format("END:   %s(%s)", m_method, m_name));
      }
   }
}
