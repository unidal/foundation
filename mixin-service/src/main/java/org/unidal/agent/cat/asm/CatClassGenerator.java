package org.unidal.agent.cat.asm;

import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.unidal.agent.cat.model.entity.EventModel;
import org.unidal.agent.cat.model.entity.MethodModel;
import org.unidal.agent.cat.model.entity.TransactionModel;
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

      if (isDebug()) {
         System.out.println(m_ctx.getClassModel());
         // ClassPrinter.print(new ClassReader(bytes));

         PrintWriter pw = new PrintWriter(System.out);

         new ClassReader(bytes).accept(new TraceClassVisitor(null, new ASMifier(), pw), ClassReader.SKIP_DEBUG);
      }

      return bytes;
   }

   private static boolean isDebug() {
      return "true".equals(System.getProperty("CAT_DEBUG"));
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
            if (isDebug()) {
               System.out.println(String.format("Visit %s%s of %s ...", name, desc, m_ctx.getClassModel().getName()));
            }

            if (method.getTransaction() != null) {
               MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

               m_ctx.setMethod(method);
               m_ctx.setLocalVariables(new LocalVariables(mv, desc, exceptions));
               m_ctx.setExpressions(new Expressions(m_ctx, mv));
               return new MethodWrapperForTransaction(m_ctx, mv);
            } else {
               // TODO
            }
         }

         return super.visitMethod(access, name, desc, signature, exceptions);
      }
   }

   private static class LocalVariables implements Opcodes {
      private MethodVisitor m_mv;

      private Type[] m_argumentTypes;

      private Type m_returnType;

      private int m_newVariables;

      private List<String> m_exceptions = new ArrayList<String>();

      public LocalVariables(MethodVisitor mv, String desc, String[] exceptions) {
         m_mv = mv;
         m_argumentTypes = Type.getArgumentTypes(desc);
         m_returnType = Type.getReturnType(desc);

         if (exceptions != null) {
            for (String exception : exceptions) {
               if (exception.equals("java/lang/RuntimeException")) {
                  continue;
               } else if (exception.equals("java/lang/Error")) {
                  continue;
               }

               m_exceptions.add(exception);
            }
         }
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

      public int indexOfException() {
         return indexOfTransaction() + 1;
      }

      public int indexOfNext() {
         m_newVariables++;

         return getArgumentSize() + 2 + m_newVariables + (hasReturn() ? 1 : 0);
      }

      public int getNewVariables() {
         return m_newVariables;
      }

      public Type[] getArgumentTypes() {
         return m_argumentTypes;
      }

      public void storeResult() {
         int index = indexOfResult();

         switch (m_returnType.getSort()) {
         case Type.VOID:
            break;
         case Type.BOOLEAN:
         case Type.CHAR:
         case Type.BYTE:
         case Type.SHORT:
         case Type.INT:
            m_mv.visitVarInsn(ISTORE, index);
            break;
         case Type.FLOAT:
            m_mv.visitVarInsn(FSTORE, index);
            break;
         case Type.LONG:
            m_mv.visitVarInsn(LSTORE, index);
            break;
         case Type.DOUBLE:
            m_mv.visitVarInsn(DSTORE, index);
            break;
         default:
            m_mv.visitVarInsn(ASTORE, index);
            break;
         }
      }

      public void loadResult() {
         int index = indexOfResult();

         switch (m_returnType.getSort()) {
         case Type.VOID:
            break;
         case Type.BOOLEAN:
         case Type.CHAR:
         case Type.BYTE:
         case Type.SHORT:
         case Type.INT:
            m_mv.visitVarInsn(ILOAD, index);
            break;
         case Type.FLOAT:
            m_mv.visitVarInsn(FLOAD, index);
            break;
         case Type.LONG:
            m_mv.visitVarInsn(LLOAD, index);
            break;
         case Type.DOUBLE:
            m_mv.visitVarInsn(DLOAD, index);
            break;
         default:
            m_mv.visitVarInsn(ALOAD, index);
            break;
         }
      }

      public void loadTransaction() {
         m_mv.visitVarInsn(ALOAD, indexOfTransaction());
      }

      public void storeTransaction() {
         m_mv.visitVarInsn(ASTORE, indexOfTransaction());
      }

      public void storeException() {
         m_mv.visitVarInsn(ASTORE, indexOfException());
      }

      public void loadException() {
         m_mv.visitVarInsn(ALOAD, indexOfException());
      }

      public void throwException() {
         m_mv.visitVarInsn(ALOAD, indexOfException());
         m_mv.visitInsn(ATHROW);
      }

      public List<String> getExceptions() {
         return m_exceptions;
      }
   }

   private static class Context {
      private ClassModel m_model;

      private ClassWriter m_writer;

      private ClassVisitor m_cv;

      private MethodModel m_method;

      private LocalVariables m_localVariables;

      private Expressions m_expressions;

      public Context(ClassModel model, byte[] classfileBuffer) {
         m_model = model;
         m_writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      }

      public void setExpressions(Expressions expressions) {
         m_expressions = expressions;
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

      public void prepare(String key) {
         m_expressions.eval(key);
      }

      public Object[] getLocalTypes() {
         Object[] types = new Object[m_localVariables.indexOfTransaction() + 1];
         int index = 1;

         types[0] = getBinaryClassName();

         for (Type type : m_localVariables.getArgumentTypes()) {
            switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
               types[index] = Opcodes.INTEGER;
               break;
            case Type.FLOAT:
               types[index] = Opcodes.FLOAT;
               break;
            case Type.LONG:
               types[index] = Opcodes.LONG;
               break;
            case Type.DOUBLE:
               types[index] = Opcodes.DOUBLE;
               break;
            case Type.ARRAY:
               types[index] = type.getDescriptor();
               break;
            case Type.OBJECT:
            default:
               types[index] = type.getInternalName();
               break;
            }

            index++;
         }

         types[m_localVariables.indexOfTransaction()] = getBinaryTransaction();
         return types;
      }
   }

   private static class Expressions implements Opcodes {
      private Context m_ctx;

      private MethodVisitor m_mv;

      private LocalVariables m_lvs;

      public Expressions(Context ctx, MethodVisitor mv) {
         m_ctx = ctx;
         m_mv = mv;
         m_lvs = ctx.getLocalVariables();
      }

      public void eval(String expr) {
         if (expr.equals("${return}")) {
            if (m_lvs.hasReturn()) {
               m_mv.visitVarInsn(ALOAD, m_lvs.indexOfResult());
            } else {
               m_mv.visitInsn(ACONST_NULL);
            }
         } else if (expr.startsWith("${arg") && expr.endsWith("}")) {
            int index = Integer.parseInt(expr.substring("${arg".length(), expr.length() - 1));

            m_mv.visitVarInsn(ALOAD, index + 1);
         } else {
            m_mv.visitLdcInsn(expr);
         }
      }
   }

   private static class MethodWrapperForTransaction extends MethodVisitor implements Opcodes {
      private Context m_ctx;

      private Label m_bizStart;

      private Label m_bizEnd;

      private Label[] m_checkedHandlers;

      private Label m_runtimeHandler;

      private Label m_errorHandler;

      private Label m_finallyHandler;

      private boolean m_return;

      private int m_maxStack;

      private int m_maxLocals;

      public MethodWrapperForTransaction(Context ctx, MethodVisitor mv) {
         super(Opcodes.ASM5, mv);

         m_ctx = ctx;
      }

      private void buildAfterReturnClause() {
         buildCheckedExceptionClause();
         buildRuntimeExceptionClause();
         buildErrorClause();
         buildFinallyClause();
      }

      private void buildBeforeReturnClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();

         lvs.storeResult();

         TransactionModel transaction = m_ctx.getMethod().getTransaction();

         buildTransactionBody(lvs, transaction.getKeys(), transaction.getValues());
         buildEventIfHave(lvs);

         mv.visitLabel(m_bizEnd);

         lvs.loadTransaction();
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "complete", "()V", true);

         lvs.loadResult();
      }

      private void buildTransactionBody(LocalVariables lvs, List<String> keys, List<String> values) {
         int index = 0;

         for (String key : keys) {
            String value = values.get(index++);

            lvs.loadTransaction();
            m_ctx.prepare(key);
            m_ctx.prepare(value);
            mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "addData",
                  "(Ljava/lang/String;Ljava/lang/Object;)V", true);
         }

         lvs.loadTransaction();
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "success", "()V", true);
      }

      private void buildEventIfHave(LocalVariables lvs) {
         EventModel event = m_ctx.getMethod().getEvent();

         if (event != null) {
            if (event.getKeys().isEmpty()) {
               m_ctx.prepare(event.getType());
               m_ctx.prepare(event.getName());
               mv.visitMethodInsn(INVOKESTATIC, m_ctx.getBinaryCat(), "logEvent",
                     "(Ljava/lang/String;Ljava/lang/String;)V", false);
            } else {

            }
         }
      }

      private void buildBeforeTryClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();

         m_bizStart = new Label();
         m_bizEnd = new Label();
         m_runtimeHandler = new Label();
         m_errorHandler = new Label();
         m_finallyHandler = new Label();

         List<String> exceptions = lvs.getExceptions();

         if (!exceptions.isEmpty()) {
            int index = 0;

            m_checkedHandlers = new Label[exceptions.size()];

            for (String exception : exceptions) {
               m_checkedHandlers[index] = new Label();
               mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_checkedHandlers[index], exception);
               index++;
            }
         }

         mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_runtimeHandler, "java/lang/RuntimeException");
         mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_errorHandler, "java/lang/Error");

         if (lvs.hasReturn()) {
            mv.visitTryCatchBlock(m_bizStart, m_bizEnd, m_finallyHandler, null);

            if (m_checkedHandlers != null) {
               mv.visitTryCatchBlock(m_checkedHandlers[0], m_finallyHandler, m_finallyHandler, null);
            } else {
               mv.visitTryCatchBlock(m_runtimeHandler, m_finallyHandler, m_finallyHandler, null);
            }
         } else {
            mv.visitTryCatchBlock(m_bizStart, m_finallyHandler, m_finallyHandler, null);
         }

         m_ctx.prepare(m_ctx.getTransactionType());
         m_ctx.prepare(m_ctx.getTransactionName());
         mv.visitMethodInsn(INVOKESTATIC, m_ctx.getBinaryCat(), "newTransaction",
               "(Ljava/lang/String;Ljava/lang/String;)Lorg/unidal/cat/message/Transaction;", false);

         lvs.storeTransaction();
         mv.visitLabel(m_bizStart);
      }

      private void buildCheckedExceptionClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();
         List<String> exceptions = lvs.getExceptions();

         if (!exceptions.isEmpty()) {
            int index = 0;

            for (String exception : exceptions) {
               Label label = m_checkedHandlers[index];

               mv.visitLabel(label);

               if (index == 0) {
                  mv.visitFrame(Opcodes.F_FULL, lvs.indexOfTransaction(), m_ctx.getLocalTypes(), 1,
                        new Object[] { exception });
               } else {
                  mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { exception });
               }

               lvs.storeException();
               lvs.loadTransaction();
               lvs.loadException();
               mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "setStatus",
                     "(Ljava/lang/Throwable;)V", true);
               lvs.throwException();
               index++;
            }
         }
      }

      private void buildErrorClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();

         mv.visitLabel(m_errorHandler);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Error" });
         lvs.storeException();
         lvs.loadTransaction();
         lvs.loadException();
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "setStatus", "(Ljava/lang/Throwable;)V",
               true);
         lvs.throwException();
      }

      private void buildFinallyClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();
         int next = lvs.indexOfNext();

         mv.visitLabel(m_finallyHandler);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Throwable" });
         mv.visitVarInsn(ASTORE, next);
         lvs.loadTransaction();
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "complete", "()V", true);
         mv.visitVarInsn(ALOAD, next);
         mv.visitInsn(ATHROW);
      }

      private void buildRuntimeExceptionClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();

         mv.visitLabel(m_runtimeHandler);

         if (m_checkedHandlers != null) {
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/RuntimeException" });
         } else {
            mv.visitFrame(Opcodes.F_FULL, lvs.indexOfTransaction(), m_ctx.getLocalTypes(), 1,
                  new Object[] { "java/lang/RuntimeException" });
         }

         lvs.storeException();
         lvs.loadTransaction();
         lvs.loadException();
         mv.visitMethodInsn(INVOKEINTERFACE, m_ctx.getBinaryTransaction(), "setStatus", "(Ljava/lang/Throwable;)V",
               true);
         lvs.throwException();
      }

      @Override
      public void visitCode() {
         super.visitCode();
         buildBeforeTryClause();
      }

      @Override
      public void visitInsn(int opcode) {
         if (opcode >= IRETURN && opcode <= RETURN) {
            m_return = true;
            buildBeforeReturnClause();
            super.visitInsn(opcode);
            buildAfterReturnClause();
         }

         super.visitInsn(opcode);
      }

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
         m_maxStack = maxStack;
         m_maxLocals = maxLocals + m_ctx.getLocalVariables().getNewVariables();
      }

      @Override
      public void visitEnd() {
         if (!m_return) {
            buildBeforeReturnClause();
            buildAfterReturnClause();
         }

         super.visitMaxs(m_maxStack + 100, m_maxLocals + 100);
         super.visitEnd();
      }
   }
}
