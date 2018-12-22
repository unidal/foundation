package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloReturnType {
   private static final String TYPE = "HelloReturnType";

   @CatTransaction(type = TYPE, name = "void helloVoid()")
   public void helloVoid() {
   }

   @CatTransaction(type = TYPE, name = "double helloDouble()")
   public double helloDouble() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "float helloFloat()")
   public float helloFloat() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "int helloInt()")
   public int helloInt() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "long helloLong()")
   public long helloLong() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "String helloString()")
   public String helloString() {
      return null;
   }

}
