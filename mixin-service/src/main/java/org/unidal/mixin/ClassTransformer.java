package org.unidal.mixin;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
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

   public synchronized void initialize() {
      if (!m_initialized.get()) {
         MixinModel soruce = m_builder.build();
         MixinModel aggregated = new MixinModelAggregator().aggregate(soruce);
         JarFile jarfile = new JarFileBuilder(aggregated).build();

         if (jarfile != null) {
            m_instrumentation.appendToSystemClassLoaderSearch(jarfile);
         }

         m_mixin = aggregated;
         m_initialized.set(true);
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
