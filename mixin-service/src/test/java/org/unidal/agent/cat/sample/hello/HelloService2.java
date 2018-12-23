package org.unidal.agent.cat.sample.hello;

import org.objectweb.asm.util.ASMifier;
import org.unidal.cat.Cat;
import org.unidal.cat.message.Transaction;

public class HelloService2 {
   public static void main(String[] args) throws Exception {
      ASMifier.main(new String[] { "-debug", HelloService2.class.getName() });
   }

   public void helloDouble(double value) {
      Transaction t = Cat.newTransaction("Hello", "void helloDouble(double value)");

      try {
         t.addData("arg0", String.valueOf(value));

         t.success();
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
