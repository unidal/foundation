package org.unidal.agent.cat;

import java.io.IOException;
import java.util.jar.JarFile;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.unidal.agent.AgentMain;
import org.unidal.agent.ClassWeaver;
import org.unidal.agent.cat.asm.CatClassGenerator;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.RootModel;

public class CatClassWeaver implements ClassWeaver {
   public static final String ID = "cat";

   private RootModel m_model;

   private CatModelBuilder m_builder = new CatModelBuilder();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JarFile initialize() {
      m_model = m_builder.build();
      return null;
   }

   @Override
   public boolean isEligible(String className) {
      ClassModel model = m_model.findClass(className);

      return model != null;
   }

   public void register(ClassModel model) {
      m_builder.register(model);
   }

   @Override
   public void register(String className) {
      try {
         ClassReader reader = new ClassReader(className.replace('.', '/'));
         CatMetaRecognizer recognizer = new CatMetaRecognizer();

         reader.accept(recognizer, ClassReader.SKIP_FRAMES + ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG);

         if (recognizer.isFound()) {
            m_builder.register(className);
         }
      } catch (Exception e) {
         // ignore it
         new RuntimeException(String.format("Unable to register class(%s)!", className), e).printStackTrace();
      }
   }

   @Override
   public byte[] weave(String className, byte[] classfileBuffer, boolean redefined) throws IOException {
      ClassModel model = m_model.findClass(className);
      byte[] result = new CatClassGenerator(model, classfileBuffer).generate(redefined);

      AgentMain.info("[CAT] Class(%s) is transformed.", className);
      return result;
   }

   private static class CatMetaRecognizer extends ClassVisitor {
      private boolean m_found;

      public CatMetaRecognizer() {
         super(Opcodes.ASM5);
      }

      public boolean isFound() {
         return m_found;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         Type type = Type.getType(desc);

         if (CatEnabled.class.getName().equals(type.getClassName())) {
            m_found = true;

            return new AnnotationVisitor(Opcodes.ASM5) {
               @Override
               public void visit(String name, Object value) {
                  if (name.equals("value") && Boolean.FALSE.equals(value)) {
                     m_found = false;
                  }
               }
            };
         }

         return null;
      }
   }
}
