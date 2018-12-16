package org.unidal.agent.cat;

import java.io.IOException;
import java.util.jar.JarFile;

import org.unidal.agent.ClassWeaver;

public class CatClassWeaver implements ClassWeaver {
   @Override
   public JarFile initialize() {
      return null;
   }

   @Override
   public boolean isEligible(String className) {
      return false;
   }

   @Override
   public void register(String className) {
   }

   @Override
   public byte[] weave(String binaryClassName, byte[] classfileBuffer, boolean redefined) throws IOException {
      return null;
   }
}
