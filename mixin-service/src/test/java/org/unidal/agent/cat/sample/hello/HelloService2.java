package org.unidal.agent.cat.sample.hello;

import java.io.IOException;

import org.unidal.cat.Cat;
import org.unidal.cat.message.Transaction;

public class HelloService2 {
   public String hello(String name) throws IOException, IllegalStateException {
      Transaction t = Cat.newTransaction("Service", "hello");

      try {
         String $return = "Hello " + name;

         t.addData("name", name);
         t.addData("return", $return);

         if ("200".equals($return) || "201".equals($return)) {
            t.success();
         } else {
            t.setStatus($return);
         }

         Cat.logEvent("Guest", name);

         String str = "test";

         System.out.println(str);
         System.in.read();

         return $return;
      } catch (IllegalStateException e) {
         t.setStatus(e);
         throw e;
      } catch (IOException e) {
         t.setStatus(e);
         throw e;
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
