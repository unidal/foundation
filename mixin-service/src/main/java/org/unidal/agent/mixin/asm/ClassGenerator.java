package org.unidal.agent.mixin.asm;

import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.unidal.agent.mixin.MixinMeta;
import org.unidal.agent.mixin.model.entity.ClassModel;
import org.unidal.agent.mixin.model.entity.FieldModel;
import org.unidal.agent.mixin.model.entity.InnerClassModel;
import org.unidal.agent.mixin.model.entity.MethodModel;
import org.unidal.agent.mixin.model.entity.SourceModel;
import org.unidal.agent.mixin.model.entity.TargetModel;

public class ClassGenerator {
   private Context m_ctx;

   private ClassReader m_base;

   public ClassGenerator(ClassModel model, byte[] classfileBuffer) {
      m_ctx = new Context(model, classfileBuffer);
      m_base = new ClassReader(classfileBuffer);
   }

   public byte[] generate(boolean redefined) throws IOException {
      for (SourceModel source : m_ctx.getClassModel().getSources()) {
         ClassReader reader = m_ctx.getClassReader(source.getName());

         reader.accept(new SourceContributor(m_ctx, source), ClassReader.SKIP_FRAMES);
      }

      m_base.accept(new BaseContributor(m_ctx, m_ctx.getTargetModel()), ClassReader.SKIP_FRAMES);

      byte[] bytes = m_ctx.getByteArray();

      if ("true".equals(System.getProperty("MIXIN_DEBUG"))) {
         System.out.println(m_ctx.getClassModel());
         // ClassPrinter.print(new ClassReader(bytes));

         PrintWriter pw = new PrintWriter(System.out);

         new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), pw), ClassReader.SKIP_DEBUG);
      }

      return bytes;
   }

   public static class BaseContributor extends ClassVisitor {
      private Context m_ctx;

      private TargetModel m_model;

      public BaseContributor(Context ctx, TargetModel model) {
         super(Opcodes.ASM5, ctx.getClassVisitor());

         m_ctx = ctx;
         m_model = model;
      }

      @Override
      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
         FieldModel field = m_ctx.findField(m_model.getName(), name);

         if (field != null) {
            return new CommonFieldMigrator(m_ctx, field, cv, access, name, desc, signature, value);
         } else {
            return super.visitField(access, name, desc, signature, value);
         }
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         MethodModel method = m_ctx.findMethod(m_model.getName(), name, desc);

         if (method != null) {
            if (method.getSourceName().equals("<clinit>")) {
               m_ctx.setMethodVisitor(cv.visitMethod(access, name, desc, signature, exceptions));

               return new StaticInitMethodMerger(m_ctx, method, true);
            } else if (method.getSourceName().equals("<init>")) {
               m_ctx.setMethodVisitor(cv.visitMethod(access, name, desc, signature, exceptions));

               return new InitMethodMerger(m_ctx, method, true);
            } else {
               return new CommonMethodMigrator(m_ctx, method, cv, access, name, desc, signature, exceptions);
            }
         }

         return super.visitMethod(access, name, desc, signature, exceptions);
      }
   }

   private static class CommonFieldMigrator extends FieldVisitor {
      public CommonFieldMigrator(Context ctx, FieldModel field, ClassVisitor cv, int access, String name, String desc,
            String signature, Object value) {
         super(Opcodes.ASM5);

         super.fv = cv.visitField(field.getAccess(), field.getName(), field.getDesc(), signature, value);
      }
   }

   private static class CommonMethodMigrator extends MethodVisitor {
      private Context m_ctx;

      private MethodModel m_method;

      public CommonMethodMigrator(Context ctx, MethodModel method, ClassVisitor cv, int access, String name,
            String desc, String signature, String[] exceptions) {
         super(Opcodes.ASM5);

         m_ctx = ctx;
         m_method = method;
         super.mv = cv.visitMethod(method.getAccess(), method.getName(), desc, signature, exceptions);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc) {
         FieldModel field = m_ctx.findField(m_method.getSourceClass(), name);

         if (field != null) {
            super.visitFieldInsn(opcode, m_ctx.getTargetClass(), field.getName(), field.getDesc());
         } else {
            super.visitFieldInsn(opcode, owner, name, desc);
         }
      }

      @Override
      public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
         String targetDesc = m_ctx.getTargetDesc(desc);

         super.visitLocalVariable(name, targetDesc, signature, start, end, index);
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
         InnerClassModel innerClass = m_ctx.findInnerClassByName(owner);
         String targetOwner = m_ctx.getTargetClass(owner);

         if (innerClass != null) {
            super.visitMethodInsn(opcode, innerClass.getName(), name, desc, itf);
         } else if (name.startsWith("$_")) {
            MethodModel method = m_ctx.findMethod(owner, name.substring(2), desc);

            super.visitMethodInsn(opcode, targetOwner, method.getSuperName(), desc, itf);
         } else {
            super.visitMethodInsn(opcode, targetOwner, name, desc, itf);
         }
      }

      @Override
      public void visitTypeInsn(int opcode, String type) {
         switch (opcode) {
         case Opcodes.NEW:
            InnerClassModel innerClass = m_ctx.findInnerClassByName(type);

            if (innerClass != null) {
               type = innerClass.getName();
            }

            break;
         }

         super.visitTypeInsn(opcode, type);
      }
   }

   private static class Context {
      private ClassModel m_model;

      private ClassWriter m_writer;

      private MethodVisitor m_mv;

      public Context(ClassModel model, byte[] classfileBuffer) {
         m_model = model;
         m_writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      }

      public FieldModel findField(String className, String name) {
         TargetModel model = m_model.getTarget();

         for (FieldModel field : model.getFields()) {
            if (name.equals(field.getSourceName()) && className.equals(field.getSourceClass())) {
               return field;
            }
         }

         return null;
      }

      public InnerClassModel findInnerClass(String innerName) {
         TargetModel model = m_model.getTarget();

         for (InnerClassModel innerClass : model.getInnerClasses()) {
            if (innerName.equals(innerClass.getInnerName())) {
               return innerClass;
            }
         }

         return null;
      }

      public InnerClassModel findInnerClassByName(String name) {
         TargetModel model = m_model.getTarget();

         for (InnerClassModel innerClass : model.getInnerClasses()) {
            if (name.equals(innerClass.getSourceName())) {
               return innerClass;
            }
         }

         return null;
      }

      public MethodModel findMethod(String className, String name, String desc) {
         TargetModel model = m_model.getTarget();

         for (MethodModel method : model.getMethods()) {
            if (name.equals(method.getSourceName()) && className.equals(method.getSourceClass())
                  && desc.equals(method.getDesc())) {
               return method;
            }
         }

         return null;
      }

      public byte[] getByteArray() {
         return m_writer.toByteArray();
      }

      public ClassModel getClassModel() {
         return m_model;
      }

      public ClassReader getClassReader(String name) throws IOException {
         return new ClassReader(name);
      }

      public ClassVisitor getClassVisitor() {
         return new CheckClassAdapter(m_writer);
      }

      public MethodVisitor getMethodVisitor() {
         return m_mv;
      }

      public String getTargetClass() {
         return m_model.getTarget().getName();
      }

      public String getTargetClass(String owner) {
         if (m_model.findSource(owner) != null) {
            return getTargetClass();
         }

         for (SourceModel source : m_model.getSources()) {
            for (InnerClassModel innerClass : source.getInnerClasses()) {
               if (innerClass.getName().equals(owner)) {
                  return getTargetClass() + "$" + innerClass.getInnerName();
               }
            }
         }

         return owner;
      }

      public String getTargetDesc(String desc) {
         try {
            Type type = Type.getType(desc);
            String name = type.getInternalName();

            for (SourceModel source : m_model.getSources()) {
               if (source.getName().equals(name)) {
                  return Type.getObjectType(getTargetClass()).getDescriptor();
               }
            }
         } catch (Exception e) {
            // ignore it
         }

         return desc;
      }

      public TargetModel getTargetModel() {
         return m_model.getTarget();
      }

      public void setMethodVisitor(MethodVisitor mv) {
         m_mv = mv;
      }
   }

   private static class InitMethodContributor extends ClassVisitor {
      private Context m_ctx;

      private MethodModel m_method;

      public InitMethodContributor(Context ctx, MethodModel method) {
         super(Opcodes.ASM5, null);

         m_ctx = ctx;
         m_method = method;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         if (name.equals("<init>")) {
            return new InitMethodMerger(m_ctx, m_method, false);
         }

         return null;
      }
   }

   private static class InitMethodMerger extends MethodVisitor {
      private Context m_ctx;

      private MethodModel m_method;

      private boolean m_target;

      private int m_maxStack;

      private int m_maxLocals;

      private boolean m_constructed;

      public InitMethodMerger(Context ctx, MethodModel method, boolean target) {
         super(Opcodes.ASM5);

         m_ctx = ctx;
         m_method = method;
         m_target = target;

         super.mv = m_ctx.getMethodVisitor();
      }

      @Override
      public void visitEnd() {
         if (m_target) {
            for (MethodModel method : m_ctx.getTargetModel().getMethods()) {
               // source constructor
               if (method.getSourceName().equals("<init>") && !method.getSourceClass().equals(m_ctx.getTargetClass())) {
                  try {
                     ClassReader reader = m_ctx.getClassReader(method.getSourceClass());

                     reader.accept(new InitMethodContributor(m_ctx, method), ClassReader.SKIP_FRAMES);
                  } catch (Exception e) {
                     // ignore it
                     e.printStackTrace();
                  }
               }
            }

            super.visitInsn(Opcodes.RETURN);
            super.visitMaxs(m_maxStack + 100, m_maxLocals + 100);
            super.visitEnd();
         }
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc) {
         FieldModel field = m_ctx.findField(m_method.getSourceClass(), name);

         if (field != null) {
            super.visitFieldInsn(opcode, m_ctx.getTargetClass(), field.getName(), field.getDesc());
         } else {
            super.visitFieldInsn(opcode, owner, name, desc);
         }
      }

      @Override
      public void visitInsn(int opcode) {
         if (opcode != Opcodes.RETURN) {
            super.visitInsn(opcode);
         }
      }

      @Override
      public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
         if (m_target && "this".equals(name)) { // to avoid duplicated local variables
            String targetDesc = m_ctx.getTargetDesc(desc);

            super.visitLocalVariable(name, targetDesc, signature, start, end, index);
         }
      }

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
         if (maxStack > m_maxStack) {
            m_maxStack = maxStack;
         }

         if (maxLocals > m_maxLocals) {
            m_maxLocals = maxLocals;
         }
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
         if (!m_target && !m_constructed && opcode == Opcodes.INVOKESPECIAL) {
            m_constructed = true;
            return; // ignore INVOKESPECIAL at second line
         }

         InnerClassModel innerClass = m_ctx.findInnerClassByName(owner);

         if (innerClass != null) {
            super.visitMethodInsn(opcode, innerClass.getName(), name, "L" + innerClass.getName() + ";", itf);
         } else {
            String targetOwner = m_ctx.getTargetClass(owner);

            super.visitMethodInsn(opcode, targetOwner, name, desc, itf);
         }
      }

      public void visitTypeInsn(int opcode, String type) {
         switch (opcode) {
         case Opcodes.NEW:
            InnerClassModel innerClass = m_ctx.findInnerClassByName(type);

            if (innerClass != null) {
               type = innerClass.getName();
            }

            break;
         }

         super.visitTypeInsn(opcode, type);
      }

      @Override
      public void visitVarInsn(int opcode, int var) {
         if (!m_target && !m_constructed && opcode == Opcodes.ALOAD) {
            return; // ignore ALOAD at first line
         }

         super.visitVarInsn(opcode, var);
      }
   }

   private static class SourceContributor extends ClassVisitor {
      private Context m_ctx;

      private SourceModel m_model;

      public SourceContributor(Context ctx, SourceModel model) {
         super(Opcodes.ASM5, ctx.getClassVisitor());

         m_ctx = ctx;
         m_model = model;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         Type type = Type.getType(desc);

         if (MixinMeta.class.getName().equals(type.getClassName())) {
            return null;
         }

         return super.visitAnnotation(desc, visible);
      }

      @Override
      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
         FieldModel field = m_ctx.findField(m_model.getName(), name);

         if (field != null) {
            return new CommonFieldMigrator(m_ctx, field, cv, access, name, desc, signature, value);
         } else {
            return null;
         }
      }

      @Override
      public void visitInnerClass(String name, String outerName, String innerName, int access) {
         InnerClassModel innerClass = m_ctx.findInnerClass(innerName);

         if (innerClass != null) {
            super.visitInnerClass(innerClass.getName(), innerClass.getOuterName(), innerName, access);
         }
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         if (name.startsWith("$_") || name.equals("<clinit>") || name.equals("<init>")) {
            // no copy
            return null;
         }

         MethodModel method = m_ctx.findMethod(m_model.getName(), name, desc);

         if (method != null) {
            return new CommonMethodMigrator(m_ctx, method, cv, access, name, desc, signature, exceptions);
         } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
         }
      }
   }

   private static class StaticInitMethodContributor extends ClassVisitor {
      private Context m_ctx;

      private MethodModel m_method;

      public StaticInitMethodContributor(Context ctx, MethodModel method) {
         super(Opcodes.ASM5, null);

         m_ctx = ctx;
         m_method = method;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         if (name.equals("<clinit>")) {
            return new StaticInitMethodMerger(m_ctx, m_method, false);
         }

         return null;
      }
   }

   private static class StaticInitMethodMerger extends MethodVisitor {
      private Context m_ctx;

      private MethodModel m_method;

      private boolean m_target;

      private int m_maxStack;

      private int m_maxLocals;

      public StaticInitMethodMerger(Context ctx, MethodModel method, boolean target) {
         super(Opcodes.ASM5);

         m_ctx = ctx;
         m_method = method;
         m_target = target;

         super.mv = m_ctx.getMethodVisitor();
      }

      @Override
      public void visitEnd() {
         if (m_target) {
            String targetClass = m_ctx.getTargetClass();

            for (MethodModel method : m_ctx.getTargetModel().getMethods()) {
               if (method.getSourceName().equals("<clinit>") && !method.getSourceClass().equals(targetClass)) {
                  try {
                     ClassReader reader = m_ctx.getClassReader(method.getSourceClass());

                     reader.accept(new StaticInitMethodContributor(m_ctx, method), ClassReader.SKIP_FRAMES);
                  } catch (Exception e) {
                     // ignore it
                     e.printStackTrace();
                  }
               }
            }

            super.visitInsn(Opcodes.RETURN);
            super.visitMaxs(m_maxStack + 100, m_maxLocals + 100);
            super.visitEnd();
         }
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc) {
         FieldModel field = m_ctx.findField(m_method.getSourceClass(), name);

         if (field != null) {
            super.visitFieldInsn(opcode, m_ctx.getTargetClass(), field.getName(), field.getDesc());
         } else {
            super.visitFieldInsn(opcode, owner, name, desc);
         }
      }

      @Override
      public void visitInsn(int opcode) {
         if (opcode != Opcodes.RETURN) {
            super.visitInsn(opcode);
         }
      }

      @Override
      public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
         String targetDesc = m_ctx.getTargetDesc(desc);

         super.visitLocalVariable(name, targetDesc, signature, start, end, index);
      }

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
         if (maxStack > m_maxStack) {
            m_maxStack = maxStack;
         }

         if (maxLocals > m_maxLocals) {
            m_maxLocals = maxLocals;
         }
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
         InnerClassModel innerClass = m_ctx.findInnerClassByName(owner);

         if (innerClass != null) {
            String targetDesc = desc.replace(innerClass.getSourceName(), innerClass.getName());

            super.visitMethodInsn(opcode, innerClass.getName(), name, targetDesc, itf);
         } else {
            String targetOwner = m_ctx.getTargetClass(owner);

            super.visitMethodInsn(opcode, targetOwner, name, desc, itf);
         }
      }

      public void visitTypeInsn(int opcode, String type) {
         switch (opcode) {
         case Opcodes.NEW:
            InnerClassModel innerClass = m_ctx.findInnerClassByName(type);

            if (innerClass != null) {
               type = innerClass.getName();
            }

            break;
         }

         super.visitTypeInsn(opcode, type);
      }
   }
}
