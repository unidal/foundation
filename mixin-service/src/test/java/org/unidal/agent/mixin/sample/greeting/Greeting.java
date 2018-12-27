package org.unidal.agent.mixin.sample.greeting;

import java.io.IOException;

import org.unidal.cat.Cat;
import org.unidal.cat.message.Transaction;

public class Greeting {
   private static boolean DEBUG = true;

   public String sayBye(String name) throws IOException {
      if (DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] sayBye: " + name);
      }

      return "Bye " + name;
   }

   public String sayHello(String name) {
      if (name == null) {
         return null;
      }

      Transaction t = Cat.newTransaction("", "");

      try {
         if (DEBUG) {
            System.out.println("[" + getClass().getSimpleName() + "] sayHello: " + name);
         }

         return "Hello " + name;
      } catch (RuntimeException e) {
         t.setStatus(e);
         throw e;
      } catch (Error e) {
         t.setStatus(e);
         throw e;
      } finally {
         t.complete();
      }
   }
}