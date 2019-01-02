package org.unidal.asm;

import java.io.IOException;

import org.objectweb.asm.util.ASMifier;

public class AsmPrinter {
   public static void print(Class<?> klass) throws IOException {
      ASMifier.main(new String[] { klass.getName() });
   }

   public static void print(String klassName) throws IOException {
      ASMifier.main(new String[] { klassName });
   }
}
