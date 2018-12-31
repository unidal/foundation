package org.unidal.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

public class ClassTransformer implements ClassFileTransformer {
   private ClassWeaverManager m_manager = new ClassWeaverManager();

   private Instrumentation m_instrumentation;

   private AtomicBoolean m_initialized = new AtomicBoolean();

   public ClassTransformer(Instrumentation instrumentation) {
      m_instrumentation = instrumentation;
   }

   public ClassWeaver getWeaver(String id) {
      ClassWeaver weaver = m_manager.findById(id);

      if (weaver == null) {
         throw new IllegalArgumentException(String.format("Unknown ClassWeaver(%d)!", id));
      }

      return weaver;
   }

   private synchronized void initialize() {
      if (!m_initialized.get()) {
         m_initialized.set(true);
         initializeClasspath();
      }
   }

   private void initializeClasspath() {
      List<JarFile> jarFiles = m_manager.initialize();

      for (JarFile jarFile : jarFiles) {
         m_instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
      }
   }

   public void register(String className) {
      m_manager.register(className);
   }

   @Override
   public byte[] transform(ClassLoader loader, String binaryClassName, Class<?> classBeingRedefined,
         ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
      initialize();

      String className = binaryClassName.replace('/', '.');
      ClassWeaver weaver = m_manager.findByClass(className);

      if (weaver != null) {
         try {
            byte[] result = weaver.weave(className, classfileBuffer, classBeingRedefined != null);

            return result;
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      return null;
   }
}
