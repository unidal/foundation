package org.unidal.agent.mixin.sample.greeting;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;
import org.unidal.asm.util.ASMifier;
import org.unidal.cat.Cat;
import org.unidal.cat.message.Transaction;

@CatEnabled
public class Greeting2 {
   private static boolean DEBUG = true;

   public static void main(String[] args) throws Exception {
      ASMifier.main(new String[] { Greeting2.class.getName() });
   }

   @CatTransaction(type = "${class}", name = "${method}", status = "${return}:200,201", //
         keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   public String sayHello(String name) {
      if (DEBUG) {
         System.out.println("[" + getClass().getSimpleName() + "] sayHello: " + name);
      }

      return "Hello " + name;
   }

   public String sayHello2(String name) {
      Transaction t = Cat.newTransaction("Greeting2", "sayHello");

      try {
         if (DEBUG) {
            System.out.println("[" + getClass().getSimpleName() + "] sayHello: " + name);
         }

         String $return = "Hello " + name;

         t.addData("name", name);
         t.addData("return", $return);

         if ("200".equals($return) || "201".equals($return)) {
            t.success();
         } else {
            t.setStatus($return);
         }

         return $return;
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