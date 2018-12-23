package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatEvent;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloAnnotation {
   private static final String TYPE = "HelloAnnotation";

   @CatEvent(type = TYPE, name = "${method}")
   public void helloEvent() {
   }

   @CatEvent(type = TYPE, name = "${method}", //
         keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   public String helloEventData(String name) {
      return name;
   }

   @CatTransaction(type = TYPE, name = "${method}")
   public void helloTransaction() {
   }

   @CatTransaction(type = TYPE, name = "${method}", //
         keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   public String helloTransactionData(String name) {
      return name;
   }

   @CatTransaction(type = TYPE, name = "${method}")
   @CatEvent(type = TYPE, name = "${method}")
   public void helloTransactionEvent() {
   }

   @CatTransaction(type = TYPE, name = "${method}")
   @CatEvent(type = TYPE, name = "${method}", //
         keys = { "name", "return" }, values = { "${arg0}", "${return}" })
   public String helloTransactionEventData(String name) {
      return name;
   }
}
