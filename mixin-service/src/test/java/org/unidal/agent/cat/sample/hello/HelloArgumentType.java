package org.unidal.agent.cat.sample.hello;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloArgumentType {
   private static final String TYPE = "HelloArgumentType";

   @CatTransaction(type = TYPE, name = "void helloBoolean(boolean value)", keys = "arg0", values = "${arg0}")
   public void helloBoolean(boolean value) {
   }

   @CatTransaction(type = TYPE, name = "void helloByte(byte value)", keys = "arg0", values = "${arg0}")
   public void helloByte(byte value) {
   }

   @CatTransaction(type = TYPE, name = "void helloChar(char value)", keys = "arg0", values = "${arg0}")
   public void helloChar(char value) {
   }

   @CatTransaction(type = TYPE, name = "void helloDouble(double value)", keys = "arg0", values = "${arg0}")
   public void helloDouble(double value) {
   }

   @CatTransaction(type = TYPE, name = "void helloFloat(float value)", keys = "arg0", values = "${arg0}")
   public void helloFloat(float value) {
   }

   @CatTransaction(type = TYPE, name = "void helloInt(int value)", keys = "arg0", values = "${arg0}")
   public void helloInt(int value) {
   }

   @CatTransaction(type = TYPE, name = "void helloLong(long value)", keys = "arg0", values = "${arg0}")
   public void helloLong(long value) {
   }

   @CatTransaction(type = TYPE, name = "void helloShort(short value)", keys = "arg0", values = "${arg0}")
   public void helloShort(short value) {
   }

   @CatTransaction(type = TYPE, name = "void helloString(String value)", keys = "arg0", values = "${arg0}")
   public void helloString(String value) {
   }
}
