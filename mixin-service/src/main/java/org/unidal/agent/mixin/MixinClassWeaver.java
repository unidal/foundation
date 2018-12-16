package org.unidal.agent.mixin;

import java.io.IOException;
import java.util.jar.JarFile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.unidal.agent.ClassWeaver;
import org.unidal.agent.mixin.asm.ClassGenerator;
import org.unidal.agent.mixin.asm.JarFileBuilder;
import org.unidal.agent.mixin.asm.MixinModelAggregator;
import org.unidal.agent.mixin.asm.MixinModelBuilder;
import org.unidal.agent.mixin.model.entity.ClassModel;
import org.unidal.agent.mixin.model.entity.MixinModel;

public class MixinClassWeaver implements ClassWeaver {
   private MixinModel m_mixin;

   private MixinModelBuilder m_builder = new MixinModelBuilder();

   @Override
   public JarFile initialize() {
      MixinModel soruce = m_builder.build();
      MixinModel aggregated = new MixinModelAggregator().aggregate(soruce);
      JarFile jarFile = new JarFileBuilder(aggregated).build();

      m_mixin = aggregated;
      return jarFile;
   }

   @Override
   public boolean isEligible(String className) {
      ClassModel model = m_mixin.findClass(className);

      return model != null;
   }

   @Override
   public void register(String className) {
      try {
         ClassReader reader = new ClassReader(className.replace('.', '/'));
         MixinMetaRecognizer recognizer = new MixinMetaRecognizer();

         reader.accept(recognizer, ClassReader.SKIP_FRAMES + ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG);

         if (recognizer.isFound()) {
            m_builder.register(className);
         }
      } catch (Exception e) {
         // ignore it
         e.printStackTrace();
      }
   }

   @Override
   public byte[] weave(String className, byte[] classfileBuffer, boolean redefined) throws IOException {
      ClassModel model = m_mixin.findClass(className);
      byte[] result = new ClassGenerator(model, classfileBuffer).generate(redefined);

      System.out.println(String.format("[Mixin] Class(%s) is transformed.", className));
      return result;
   }

   private static class MixinMetaRecognizer extends ClassVisitor {
      private boolean m_found;

      private String m_targetDesc;

      public MixinMetaRecognizer() {
         super(Opcodes.ASM5);

         m_targetDesc = "L" + MixinMeta.class.getName().replace('.', '/') + ";";
      }

      public boolean isFound() {
         return m_found;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         if (desc.equals(m_targetDesc)) {
            m_found = true;
         }

         return null;
      }
   }
}
