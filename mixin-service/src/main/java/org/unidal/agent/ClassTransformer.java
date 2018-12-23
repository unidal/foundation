package org.unidal.agent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
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

   private synchronized void initialize() {
      if (!m_initialized.get()) {
         initializeClasspath();
         m_initialized.set(true);
      }
   }

   private void initializeClasspath() {
      List<JarFile> jarFiles = m_manager.initialize();

      for (JarFile jarFile : jarFiles) {
         m_instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
      }

      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      if (cl instanceof URLClassLoader) {
         URL[] urls = ((URLClassLoader) cl).getURLs();

         for (URL url : urls) {
            String path = url.getPath();

            if (path.endsWith(".jar")) {
               boolean system = path.contains("/jre/lib/") //
                     || path.contains("agent-") //
                     || path.contains("mixin-") //
                     || path.contains("/asm-") //
                     || path.contains("/junit-");

               if (!system) {
                  try {
                     m_instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(path));
                  } catch (IOException e) {
                     e.printStackTrace();
                  }
               }
            }
         }
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
      ClassWeaver weaver = m_manager.find(className);

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