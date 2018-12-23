package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloReturnType {
   private static final String TYPE = "HelloReturnType";

   @CatTransaction(type = TYPE, name = "boolean helloBoolean()", keys = "return", values = "${return}")
   public boolean helloBoolean() {
      return false;
   }

   @CatTransaction(type = TYPE, name = "byte helloByte()", keys = "return", values = "${return}")
   public byte helloByte() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "char helloChar()", keys = "return", values = "${return}")
   public char helloChar() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "double helloDouble()", keys = "return", values = "${return}")
   public double helloDouble() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "float helloFloat()", keys = "return", values = "${return}")
   public float helloFloat() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "int helloInt()", keys = "return", values = "${return}")
   public int helloInt() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "long helloLong()", keys = "return", values = "${return}")
   public long helloLong() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "short helloShort()", keys = "return", values = "${return}")
   public short helloShort() {
      return 0;
   }

   @CatTransaction(type = TYPE, name = "String helloString()", keys = "return", values = "${return}")
   public String helloString() {
      return null;
   }

   @CatTransaction(type = TYPE, name = "void helloVoid()", keys = "return", values = "${return}")
   public void helloVoid() {
   }
}
