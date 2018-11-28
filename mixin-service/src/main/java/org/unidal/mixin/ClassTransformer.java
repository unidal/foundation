package org.unidal.mixin;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

import org.unidal.mixin.asm.ClassGenerator;
import org.unidal.mixin.asm.JarFileBuilder;
import org.unidal.mixin.asm.MixinModelAggregator;
import org.unidal.mixin.asm.MixinModelBuilder;
import org.unidal.mixin.model.entity.ClassModel;
import org.unidal.mixin.model.entity.MixinModel;

public class ClassTransformer implements ClassFileTransformer {
   private Instrumentation m_instrumentation;

   private MixinModel m_mixin;

   private MixinModelBuilder m_builder = new MixinModelBuilder();

   private Set<String> m_transformed = new HashSet<String>();

   private AtomicBoolean m_initialized = new AtomicBoolean();

   public ClassTransformer(Instrumentation instrumentation) {
      m_instrumentation = instrumentation;
   }

   private synchronized void initialize() {
      if (!m_initialized.get()) {
         MixinModel soruce = m_builder.build();
         MixinModel aggregated = new MixinModelAggregator().aggregate(soruce);

         m_mixin = aggregated;
         initializeClasspath();
         m_initialized.set(true);
      }
   }

   private void initializeClasspath() {
      JarFile jarfile = new JarFileBuilder(m_mixin).build();

      if (jarfile != null) {
         m_instrumentation.appendToBootstrapClassLoaderSearch(jarfile);
      }

      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      if (cl instanceof URLClassLoader) {
         URL[] urls = ((URLClassLoader) cl).getURLs();

         for (URL url : urls) {
            String path = url.getPath();

            if (path.endsWith(".jar")) {
               if (path.contains("/jre/lib/") || path.contains("agent-") || path.contains("mixin-")
                     || path.contains("/asm-") || path.contains("/junit-")) {
                  continue;
               }

               try {
                  m_instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(path));
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }
         }
      }
   }

   public void register(String mixinClass) {
      m_builder.register(mixinClass);
   }

   @Override
   public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
         ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
      initialize();

      String name = className.replace('/', '.');
      ClassModel model = m_mixin.findClass(name);

      if (model != null) { // within scope of mixin
         try {
            boolean isRedefine = classBeingRedefined != null;

            if (isRedefine && !m_transformed.contains(name)) {
               m_transformed.add(name);

               m_instrumentation.retransformClasses(classBeingRedefined);
            } else {
               byte[] result = new ClassGenerator(model, classfileBuffer).generate(isRedefine);

               return result;
            }
         } catch (Throwable e) {
            e.printStackTrace();
         }
      }

      return null;
   }

   public void unregister(String mixinClass) {
      m_builder.unregister(mixinClass);
   }
}
