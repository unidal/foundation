package org.unidal.agent.mixin;

import java.io.IOException;
import java.util.jar.JarFile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.unidal.agent.AgentMain;
import org.unidal.agent.ClassWeaver;
import org.unidal.agent.mixin.asm.MixinClassGenerator;
import org.unidal.agent.mixin.asm.MixinJarFileBuilder;
import org.unidal.agent.mixin.model.entity.ClassModel;
import org.unidal.agent.mixin.model.entity.MixinModel;

public class MixinClassWeaver implements ClassWeaver {
   public static final String ID = "mixin";

   private MixinModel m_mixin;

   private MixinModelBuilder m_builder = new MixinModelBuilder();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JarFile initialize() {
      MixinModel soruce = m_builder.build();
      MixinModel aggregated = new MixinModelAggregator().aggregate(soruce);
      JarFile jarFile = new MixinJarFileBuilder(aggregated).build();

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
      byte[] result = new MixinClassGenerator(model, classfileBuffer).generate(redefined);

      AgentMain.info("[Mixin] Class(%s) is transformed.", className);
      return result;
   }

   private static class MixinMetaRecognizer extends ClassVisitor {
      private boolean m_found;

      public MixinMetaRecognizer() {
         super(Opcodes.ASM5);
      }

      public boolean isFound() {
         return m_found;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         Type type = Type.getType(desc);

         if (MixinMeta.class.getName().equals(type.getClassName())) {
            m_found = true;
         }

         return null;
      }
   }
}
