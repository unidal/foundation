package org.unidal.agent.cat.sample.hello;

import java.io.IOException;

import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatTransaction;

@CatEnabled
public class HelloException {
   private static final String TYPE = "HelloException";

   @CatTransaction(type = TYPE, name = "void helloRuntimeException()")
   public void helloRuntimeException() throws RuntimeException {
      throw new IllegalArgumentException();
   }

   @CatTransaction(type = TYPE, name = "void helloError()")
   public void helloError() throws Error {
      throw new AssertionError();
   }

   @CatTransaction(type = TYPE, name = "void helloException()")
   public void helloException() throws Exception {
      throw new Exception();
   }

   @CatTransaction(type = TYPE, name = "void helloThrowable()")
   public void helloThrowable() throws Throwable {
      throw new Throwable();
   }

   @CatTransaction(type = TYPE, name = "void helloIOException()")
   public void helloIOException() throws IOException {
      throw new IOException();
   }
}
