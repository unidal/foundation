package org.unidal.agent.mixin.sample.greeting;

import java.io.IOException;

import org.unidal.agent.mixin.MixinMeta;

@MixinMeta(targetClassName = "org.unidal.agent.mixin.sample.greeting.Greeting")
public class GreetingMixin2 {
   private static Helper m_helper = new Helper();

   private String $_sayBye(String name) {
      return null;
   }

   private String $_sayHello(String name) {
      return null;
   }

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

   public static class Helper {
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