package org.unidal.agent.mixin.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.unidal.agent.AgentMain;
import org.unidal.agent.mixin.model.entity.InnerClassModel;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.entity.SourceModel;
import org.unidal.agent.mixin.model.entity.TargetModel;
import org.unidal.agent.mixin.model.transform.BaseVisitor;

public class MixinJarFileBuilder {
   private MixinModel m_model;

   public MixinJarFileBuilder(MixinModel model) {
      m_model = model;
   }

   public JarFile build() {
      try {
         File tmpJar = File.createTempFile("mixin-", ".jar");
         JarOutputStream out = new JarOutputStream(new FileOutputStream(tmpJar));

         tmpJar.deleteOnExit();

         try {
            m_model.accept(new Builder(out));
         } finally {
            out.close();
         }

         return new JarFile(tmpJar);
      } catch (Throwable e) {
         e.printStackTrace();
      }

      return null;
   }

   private static class Builder extends BaseVisitor {
      private JarOutputStream m_out;

      private String m_targetName;

      public Builder(JarOutputStream out) {
         m_out = out;
      }

      private void addToJar(String path, byte[] content) {
         try {
            JarEntry entry = new JarEntry(path);

            m_out.putNextEntry(entry);
            m_out.write(content);
            m_out.closeEntry();
         } catch (Throwable e) {
            throw new IllegalStateException("Error when adding to mixin jar for " + path, e);
         }

         if (AgentMain.isDebug()) {
            PrintWriter pw = new PrintWriter(System.out);

            new ClassReader(content).accept(new TraceClassVisitor(null, new ASMifier(), pw), ClassReader.SKIP_DEBUG);
         }
      }

      @Override
      public void visitInnerClass(InnerClassModel innerClass) {
         if (!m_targetName.equals(innerClass.getSourceOuterName())) {
            try {
               byte[] bytes = new ClassGenerator(innerClass).generate();

               addToJar(innerClass.getName() + ".class", bytes);
            } catch (Throwable e) {
               e.printStackTrace();
            }
         }
      }

      @Override
      public void visitSource(SourceModel source) {
         // do nothing
      }

      @Override
      public void visitTarget(TargetModel target) {
         m_targetName = target.getName();

         super.visitTarget(target);
      }
   }

   private static class ClassGenerator {
      private InnerClassModel m_model;

      public ClassGenerator(InnerClassModel model) {
         m_model = model;
      }

      public byte[] generate() throws IOException {
         ClassReader reader = new ClassReader(m_model.getSourceName());
         ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
         CheckClassAdapter cv = new CheckClassAdapter(writer);

         reader.accept(new ClassMigrator(cv, m_model), Opcodes.ASM5);
         return writer.toByteArray();
      }
   }

   private static class ClassMigrator extends ClassVisitor {
      private InnerClassModel m_model;

      public ClassMigrator(ClassVisitor cv, InnerClassModel model) {
         super(Opcodes.ASM5, cv);
         m_model = model;
      }

      @Override
      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
         super.visit(version, access, m_model.getName(), signature, superName, interfaces);
      }

      @Override
      public void visitInnerClass(String name, String outerName, String innerName, int access) {
         super.visitInnerClass(m_model.getName(), m_model.getOuterName(), innerName, access);
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         return new MethodMigrator(m_model, cv, access, name, desc, signature, exceptions);
      }

      @Override
      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
         desc = desc.replace(m_model.getSourceOuterName(), m_model.getOuterName());

         return super.visitField(access, name, desc, signature, value);
      }
   }

   private static class MethodMigrator extends MethodVisitor {
      private InnerClassModel m_model;

      public MethodMigrator(InnerClassModel model, ClassVisitor cv, int access, String name, String desc,
            String signature, String[] exceptions) {
         super(Opcodes.ASM5);

         m_model = model;

         desc = desc.replace(m_model.getSourceOuterName(), m_model.getOuterName());

         if ("<init>".equals(name)) {
            super.mv = cv.visitMethod(access, name, desc, signature, exceptions);
         } else {
            super.mv = cv.visitMethod(access, name, desc, signature, exceptions);
         }
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc) {
         if (owner.contains(m_model.getSourceOuterName())) {
            owner = owner.replace(m_model.getSourceOuterName(), m_model.getOuterName());
         }

         if (desc.contains(m_model.getSourceOuterName())) {
            desc = desc.replace(m_model.getSourceOuterName(), m_model.getOuterName());
         }

         super.visitFieldInsn(opcode, owner, name, desc);
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
         if (owner.contains(m_model.getSourceOuterName())) {
            owner = owner.replace(m_model.getSourceOuterName(), m_model.getOuterName());
         }

         if (desc.contains(m_model.getSourceOuterName())) {
            desc = desc.replace(m_model.getSourceOuterName(), m_model.getOuterName());
         }

         super.visitMethodInsn(opcode, owner, name, desc, itf);
      }

      @Override
      public void visitTypeInsn(int opcode, String type) {
         if (type.contains(m_model.getSourceOuterName())) {
            type = type.replace(m_model.getSourceOuterName(), m_model.getOuterName());
         }

         super.visitTypeInsn(opcode, type);
      }
   }
}
