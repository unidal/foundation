package org.unidal.agent.cat.asm;

import java.io.PrintWriter;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.MethodModel;
import org.unidal.cat.Cat;
import org.unidal.cat.message.Transaction;

public class CatClassGenerator {
   private Context m_ctx;

   private ClassReader m_reader;

   public CatClassGenerator(ClassModel model, byte[] classfileBuffer) {
      m_ctx = new Context(model, classfileBuffer);
      m_reader = new ClassReader(classfileBuffer);
   }

   public byte[] generate(boolean redefined) {
      m_reader.accept(new ClassWrapper(m_ctx), ClassReader.SKIP_FRAMES);

      byte[] bytes = m_ctx.getByteArray();

      if ("true".equals(System.getProperty("CAT_DEBUG"))) {
         System.out.println(m_ctx.getClassModel());
         // ClassPrinter.print(new ClassReader(bytes));

         PrintWriter pw = new PrintWriter(System.out);

         new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), pw), ClassReader.SKIP_DEBUG);
      }

      return bytes;
   }

   private static class ClassWrapper extends ClassVisitor {
      private Context m_ctx;

      public ClassWrapper(Context ctx) {
         super(Opcodes.ASM5, ctx.getClassVisitor());

         m_ctx = ctx;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         MethodModel method = m_ctx.findMethod(name, desc);

         if (method != null) {
            if (method.getTransaction() != null) {
               m_ctx.setMethod(method);
               m_ctx.setLocalVariables(new LocalVariables(desc));
               return new MethodWrapperForTransaction(m_ctx, access, name, desc, signature, exceptions);
            } else {
               // TODO
            }
         }

         return super.visitMethod(access, name, desc, signature, exceptions);
      }
   }

   private static class LocalVariables {
      private Type[] m_argumentTypes;

      private Type m_returnType;

      private int m_variables;

      public LocalVariables(String desc) {
         m_argumentTypes = Type.getArgumentTypes(desc);
         m_returnType = Type.getReturnType(desc);
      }

      public int getArgumentSize() {
         return m_argumentTypes.length;
      }

      public boolean hasReturn() {
         return m_returnType.getSize() > 0;
      }

      public int indexOfResult() {
         return getArgumentSize() + 2;
      }

      public int indexOfTransaction() {
         return getArgumentSize() + 1;
      }

      public int indexOfReturn() {
         return indexOfResult() + m_variables;
      }

      public int indexOf(String expr) {
         if (expr.equals("${return}")) {
            return indexOfResult();
         } else if (expr.startsWith("${arg") && expr.endsWith("}")) {
            int index = Integer.parseInt(expr.substring("${arg".length(), expr.length() - 1));

            return index + 1;
         }

         throw new IllegalArgumentException(String.format("Unknown expression(%s)!", expr));
      }
   }

   private static class Context {
      private ClassModel m_model;

      private ClassWriter m_writer;

      private ClassVisitor m_cv;

      private MethodModel m_method;

      private LocalVariables m_localVariables;

      public Context(ClassModel model, byte[] classfileBuffer) {
         m_model = model;
         m_writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      }

      public void setMethod(MethodModel method) {
         m_method = method;
      }

      public void setLocalVariables(LocalVariables localVariables) {
         m_localVariables = localVariables;
      }

      public MethodModel findMethod(String name, String desc) {
         for (MethodModel method : m_model.getMethods()) {
            if (name.equals(method.getName()) && desc.equals(method.getDesc())) {
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

      public ClassVisitor getClassVisitor() {
         if (m_cv == null) {
            m_cv = new CheckClassAdapter(m_writer);
         }

         return m_cv;
      }

      public String getBinaryClassName() {
         return m_model.getName().replace('.', '/');
      }

      public String getBinaryTransaction() {
         return Transaction.class.getName().replace('.', '/');
      }

      public LocalVariables getLocalVariables() {
         return m_localVariables;
      }

      public String getBinaryCat() {
         return Cat.class.getName().replace('.', '/');
      }

      public String getTransactionType() {
         return m_method.getTransaction().getType();
      }

      public String getTransactionName() {
         return m_method.getTransaction().getName();
      }

      public MethodModel getMethod() {
         return m_method;
      }
   }

   private static class MethodWrapperForTransaction extends MethodVisitor implements Opcodes {
      private Context m_ctx;

      private String[] m_exceptions;

      private Label m_bizStart;

      private Label m_bizEnd;

      private Label[] m_checkedHandlers;

      private Label m_runtimeHandler;

      private Label m_errorHandler;

      private Label m_finallyHandler;

      private int m_maxStack;

      private int m_maxLocals;

      public MethodWrapperForTransaction(Context ctx, int access, String name, String desc, String signature,
            String[] exceptions) {
         super(Opcodes.ASM5);

         m_ctx = ctx;
         m_exceptions = exceptions;
         super.mv = m_ctx.getClassVisitor().visitMethod(access, name, desc, signature, exceptions);
      }

      private void buildAfterReturnClause() {
         buildCheckedExceptionClause();
         buildRuntimeExceptionClause();
         buildErrorClause();
         buildFinallyClause();
      }

      private void buildBeforeReturnClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();

         if (lvs.hasReturn()) {
            mv.visitVarInsn(ASTORE, lvs.indexOfResult());
         }

         MethodModel method = m_ctx.getMethod();
         List<String> keys = method.getTransaction().getKeys();
         List<String> values = method.getTransaction().getValues();
         int index = 0;

         for (String key : keys) {
            String value = values.get(index++);

            mv.visitVarInsn(ALOAD, lvs.indexOfTransaction());
            mv.visitLdcInsn(key);
            mv.visitVarInsn(ALOAD, lvs.indexOf(value));
            mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "addData",
                  "(Ljava/lang/String;Ljava/lang/Object;)V", true);
         }

         mv.visitLdcInsn("200");
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);

         Label l5 = new Label();
         mv.visitJumpInsn(IFNE, l5);
         mv.visitLdcInsn("201");
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);

         Label l6 = new Label();
         mv.visitJumpInsn(IFEQ, l6);
         mv.visitLabel(l5);
         mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] { m_ctx.getBinaryTransaction(), "java/lang/String" }, 0, null);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "success", "()V", true);

         Label l7 = new Label();
         mv.visitJumpInsn(GOTO, l7);
         mv.visitLabel(l6);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "setStatus", "(Ljava/lang/String;)V", true);

         mv.visitLabel(l7);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitLdcInsn("Guest");
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKESTATIC, m_ctx.getBinaryCat(), "logEvent", "(Ljava/lang/String;Ljava/lang/String;)V",
               false);

         mv.visitVarInsn(ALOAD, lvs.indexOfResult());
         mv.visitVarInsn(ASTORE, lvs.indexOfReturn());
         mv.visitLabel(m_bizEnd);

         mv.visitVarInsn(ALOAD, lvs.indexOfTransaction());
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "complete", "()V", true);
         mv.visitVarInsn(ALOAD, lvs.indexOfReturn());
      }

      private void buildBeforeTryClause() {
         m_bizStart = new Label();
         m_bizEnd = new Label();
         m_runtimeHandler = new Label();
         m_errorHandler = new Label();
         m_finallyHandler = new Label();

         if (m_exceptions != null) {
            for (int i = 0; i < m_exceptions.length; i++) {
               String exception = m_exceptions[i];

               m_checkedHandlers[i] = new Label();
               mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_checkedHandlers[i], exception);
            }
         }

         mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_runtimeHandler, "java/lang/RuntimeException");
         mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_errorHandler, "java/lang/Error");
         mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_finallyHandler, null);

         if (m_checkedHandlers != null) {
            mv.visitTryCatchBlock(m_checkedHandlers[0], m_finallyHandler, m_finallyHandler, null);
         } else {
            mv.visitTryCatchBlock(m_runtimeHandler, m_finallyHandler, m_finallyHandler, null);
         }

         mv.visitLdcInsn(m_ctx.getTransactionType());
         mv.visitLdcInsn(m_ctx.getTransactionName());
         mv.visitMethodInsn(INVOKESTATIC, m_ctx.getBinaryCat(), "newTransaction",
               "(Ljava/lang/String;Ljava/lang/String;)Lorg/unidal/cat/message/Transaction;", false);
         mv.visitVarInsn(ASTORE, 2);
         mv.visitLabel(m_bizStart);
      }

      private void buildCheckedExceptionClause() {
         if (m_exceptions != null) {
            for (int i = 0; i < m_exceptions.length; i++) {
               String exception = m_exceptions[i];
               Label label = m_checkedHandlers[i];

               mv.visitLabel(label);

               if (i == 0) {
                  mv.visitFrame(Opcodes.F_FULL, 3, new Object[] { m_ctx.getBinaryClassName(), "java/lang/String", //
                        m_ctx.getBinaryTransaction() }, 1, new Object[] { exception });
               } else {
                  mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { exception });
               }

               mv.visitVarInsn(ASTORE, 3);
               mv.visitVarInsn(ALOAD, 2);
               mv.visitVarInsn(ALOAD, 3);
               mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "setStatus",
                     "(Ljava/lang/Throwable;)V", true);
               mv.visitVarInsn(ALOAD, 3);
               mv.visitInsn(ATHROW);
            }
         }
      }

      private void buildErrorClause() {
         mv.visitLabel(m_errorHandler);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Error" });
         mv.visitVarInsn(ASTORE, 3);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "setStatus", "(Ljava/lang/Throwable;)V",
               true);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitInsn(ATHROW);
      }

      private void buildFinallyClause() {
         mv.visitLabel(m_finallyHandler);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Throwable" });
         mv.visitVarInsn(ASTORE, 4);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "complete", "()V", true);
         mv.visitVarInsn(ALOAD, 4);
         mv.visitInsn(ATHROW);
      }

      private void buildRuntimeExceptionClause() {
         mv.visitLabel(m_runtimeHandler);

         if (m_checkedHandlers != null) {
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/RuntimeException" });
         } else {
            mv.visitFrame(Opcodes.F_FULL, 3,
                  new Object[] { m_ctx.getBinaryClassName(), "java/lang/String", m_ctx.getBinaryTransaction() }, 1,
                  new Object[] { "java/lang/RuntimeException" });
         }

         mv.visitVarInsn(ASTORE, 3);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "setStatus", "(Ljava/lang/Throwable;)V",
               true);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitInsn(ATHROW);
      }

      @Override
      public void visitCode() {
         super.visitCode();
         buildBeforeTryClause();
      }

      @Override
      public void visitInsn(int opcode) {
         if (opcode == ARETURN) {
            buildBeforeReturnClause();
            super.visitInsn(opcode);
            buildAfterReturnClause();
         }

         super.visitInsn(opcode);
      }

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
         m_maxStack = maxStack;
         m_maxLocals = maxLocals;
      }

      @Override
      public void visitEnd() {
         super.visitMaxs(m_maxStack, m_maxLocals + 100);
         super.visitEnd();
      }
   }
}
