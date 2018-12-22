package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloArgumentType {
   private static final String TYPE = "HelloArgumentType";

   @CatTransaction(type = TYPE, name = "void helloVoid(String name)")
   public void helloVoid(String name) {
   }

   @CatTransaction(type = TYPE, name = "double helloDouble(double value)")
   public double helloDouble(double value) {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "float helloFloat(float value)")
   public float helloFloat(float value) {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "int helloInt(int value)")
   public int helloInt(int value) {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "long helloLong(long value)")
   public long helloLong(long value) {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "String helloString(String name)")
   public String helloString(String name) {
      return null;
   }
}
