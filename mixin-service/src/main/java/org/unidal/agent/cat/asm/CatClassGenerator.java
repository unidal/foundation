package org.unidal.agent.cat.asm;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.AnnotationVisitor;
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
import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatEvent;
import org.unidal.agent.cat.CatTransaction;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.EventModel;
import org.unidal.agent.cat.model.entity.MethodModel;
import org.unidal.agent.cat.model.entity.TransactionModel;
import org.unidal.cat.Cat;
import org.unidal.cat.message.Event;
import org.unidal.cat.message.Transaction;

public class CatClassGenerator {
   private Context m_ctx;

   private ClassReader m_reader;

   public CatClassGenerator(ClassModel model, byte[] classfileBuffer) {
      m_ctx = new Context(model, classfileBuffer);
      m_reader = new ClassReader(classfileBuffer);
   }

   private static boolean isDebug() {
      return "true".equals(System.getProperty("CAT_DEBUG"));
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

   private static class ClassWrapper extends ClassVisitor {
      private Context m_ctx;

      public ClassWrapper(Context ctx) {
         super(Opcodes.ASM5, ctx.getClassVisitor());

         m_ctx = ctx;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         Type type = Type.getType(desc);

         if (CatEnabled.class.getName().equals(type.getClassName())) {
            return null; // delete it
         }

         return super.visitAnnotation(desc, visible);
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

               m_ctx.prepare(method, mv, desc, exceptions);
               return new MethodWrapperForTransaction(m_ctx, mv);
            } else if (method.getEvent() != null) {
               MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

               m_ctx.prepare(method, mv, desc, exceptions);
               return new MethodWrapperForEvent(m_ctx, mv);
            }
         }

         return super.visitMethod(access, name, desc, signature, exceptions);
      }
   }

   private static class Context implements Opcodes {
      private ClassModel m_model;

      private ClassWriter m_writer;

      private ClassVisitor m_cv;

      private MethodModel m_method;

      private MethodVisitor m_mv;

      private LocalVariables m_localVariables;

      private Expressions m_expressions;

      public Context(ClassModel model, byte[] classfileBuffer) {
         m_model = model;
         m_writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      }

      public void eventAddData(String key, String value) {
         String desc = "(Ljava/lang/String;Ljava/lang/Object;)V";

         m_localVariables.loadEvent();
         m_expressions.eval(key);
         m_expressions.eval(value);
         m_mv.visitMethodInsn(INVOKEINTERFACE, getBinaryEvent(), "addData", desc, true);
      }

      public void eventComplete() {
         String desc = "()V";

         m_localVariables.loadEvent();
         m_mv.visitMethodInsn(INVOKEINTERFACE, getBinaryEvent(), "complete", desc, true);
      }

      public void eventIfHave() {
         EventModel event = m_method.getEvent();

         if (event != null) {
            if (event.getKeys().isEmpty()) {
               String desc = "(Ljava/lang/String;Ljava/lang/String;)V";

               m_expressions.eval(event.getType());
               m_expressions.eval(event.getName());
               m_mv.visitMethodInsn(INVOKESTATIC, getBinaryCat(), "logEvent", desc, false);
            } else {
               eventStart();

               List<String> keys = event.getKeys();
               List<String> values = event.getValues();
               int index = 0;

               for (String key : keys) {
                  String value = values.get(index++);

                  eventAddData(key, value);
               }

               eventSuccess();
               eventComplete();
            }
         }
      }

      public void eventStart() {
         String desc = "(Ljava/lang/String;Ljava/lang/String;)Lorg/unidal/cat/message/Event;";

         m_expressions.eval(m_method.getEvent().getType());
         m_expressions.eval(m_method.getEvent().getName());
         m_mv.visitMethodInsn(INVOKESTATIC, getBinaryCat(), "newEvent", desc, false);
         m_localVariables.storeEvent();
      }

      public void eventSuccess() {
         String desc = "()V";

         m_localVariables.loadEvent();
         m_mv.visitMethodInsn(INVOKEINTERFACE, getBinaryEvent(), "success", desc, true);
      }

      public MethodModel findMethod(String name, String desc) {
         for (MethodModel method : m_model.getMethods()) {
            if (name.equals(method.getName()) && desc.equals(method.getDesc())) {
               return method;
            }
         }

         return null;
      }

      public String getBinaryCat() {
         return Cat.class.getName().replace('.', '/');
      }

      public String getBinaryClassName() {
         return m_model.getName().replace('.', '/');
      }

      public String getBinaryEvent() {
         return Event.class.getName().replace('.', '/');
      }

      public String getBinaryTransaction() {
         return Transaction.class.getName().replace('.', '/');
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

      public LocalVariables getLocalVariables() {
         return m_localVariables;
      }

      public MethodModel getMethod() {
         return m_method;
      }

      public void prepare(MethodModel method, MethodVisitor mv, String desc, String[] exceptions) {
         m_method = method;
         m_mv = mv;
         m_localVariables = new LocalVariables(mv, desc, exceptions);
         m_expressions = new Expressions(this, mv);
      }

      public void throwException() {
         m_mv.visitVarInsn(ALOAD, m_localVariables.indexOfException());
         m_mv.visitInsn(ATHROW);
      }

      public void transactionAddData() {
         TransactionModel transaction = m_method.getTransaction();
         List<String> keys = transaction.getKeys();
         List<String> values = transaction.getValues();
         int index = 0;

         for (String key : keys) {
            String value = values.get(index++);

            transactionAddData(key, value);
         }
      }

      public void transactionAddData(String key, String value) {
         String desc = "(Ljava/lang/String;Ljava/lang/Object;)V";

         m_localVariables.loadTransaction();
         m_expressions.eval(key);
         m_expressions.eval(value);
         m_mv.visitMethodInsn(INVOKEINTERFACE, getBinaryTransaction(), "addData", desc, true);
      }

      public void transactionComplete() {
         String desc = "()V";

         m_localVariables.loadTransaction();
         m_mv.visitMethodInsn(INVOKEINTERFACE, getBinaryTransaction(), "complete", desc, true);
      }

      public void transactionSetStatus() {
         String desc = "(Ljava/lang/Throwable;)V";

         m_localVariables.loadTransaction();
         m_localVariables.loadException();
         m_mv.visitMethodInsn(INVOKEINTERFACE, getBinaryTransaction(), "setStatus", desc, true);
      }

      public void transactionStart() {
         String desc = "(Ljava/lang/String;Ljava/lang/String;)Lorg/unidal/cat/message/Transaction;";

         m_expressions.eval(m_method.getTransaction().getType());
         m_expressions.eval(m_method.getTransaction().getName());
         m_mv.visitMethodInsn(INVOKESTATIC, getBinaryCat(), "newTransaction", desc, false);
         m_localVariables.storeTransaction();
      }

      public void transactionSuccess() {
         String desc = "()V";

         m_localVariables.loadTransaction();
         m_mv.visitMethodInsn(INVOKEINTERFACE, getBinaryTransaction(), "success", desc, true);
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
               loadVariableInString(m_lvs.getReturnType(), m_lvs.indexOfResult());
            } else {
               m_mv.visitInsn(ACONST_NULL);
            }
         } else if (expr.startsWith("${arg") && expr.endsWith("}")) {
            try {
               int index = Integer.parseInt(expr.substring("${arg".length(), expr.length() - 1));

               if (index >= 0 && index < m_lvs.getArgumentSize()) {
                  Type type = m_lvs.getArgumentTypes()[index];

                  loadVariableInString(type, index + 1);
                  return;
               }
            } catch (NumberFormatException e) {
               // ignore it
            }

            m_mv.visitLdcInsn(expr);
         } else if (expr.equals("${method}")) {
            m_mv.visitLdcInsn(m_ctx.getMethod().getName());
         } else if (expr.equals("${class}")) {
            m_mv.visitLdcInsn(m_ctx.getClassModel().getName());
         } else {
            m_mv.visitLdcInsn(expr);
         }
      }

      private void loadVariableInString(Type type, int index) {
         switch (type.getSort()) {
         case Type.VOID:
            break;
         case Type.BOOLEAN:
            m_mv.visitVarInsn(ILOAD, index);
            m_mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Z)Ljava/lang/String;", false);
            break;
         case Type.CHAR:
            m_mv.visitVarInsn(ILOAD, index);
            m_mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(C)Ljava/lang/String;", false);
            break;
         case Type.BYTE:
         case Type.SHORT:
         case Type.INT:
            m_mv.visitVarInsn(ILOAD, index);
            m_mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
            break;
         case Type.FLOAT:
            m_mv.visitVarInsn(FLOAD, index);
            m_mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(F)Ljava/lang/String;", false);
            break;
         case Type.LONG:
            m_mv.visitVarInsn(LLOAD, index);
            m_mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(J)Ljava/lang/String;", false);
            break;
         case Type.DOUBLE:
            m_mv.visitVarInsn(DLOAD, index);
            m_mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(D)Ljava/lang/String;", false);
            break;
         default:
            m_mv.visitVarInsn(ALOAD, index);
            m_mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;",
                  false);
            break;
         }
      }
   }

   private static class LocalVariables implements Opcodes {
      private MethodVisitor m_mv;

      private Type[] m_argumentTypes;

      private Type m_returnType;

      private int m_newVariables;

      private List<String> m_exceptions = new ArrayList<String>();

      private int m_indexOfEvent;

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

      public Type[] getArgumentTypes() {
         return m_argumentTypes;
      }

      public List<String> getExceptions() {
         return m_exceptions;
      }

      public int getNewVariables() {
         return m_newVariables;
      }

      public Type getReturnType() {
         return m_returnType;
      }

      public boolean hasReturn() {
         return m_returnType.getSize() > 0;
      }

      public int indexOfEvent() {
         if (m_indexOfEvent == 0) {
            m_indexOfEvent = indexOfNext();
         }

         return m_indexOfEvent;
      }

      public int indexOfException() {
         return indexOfTransaction() + 1;
      }

      public int indexOfNext() {
         m_newVariables++;

         if (hasReturn()) {
            return getArgumentSize() + 3 + m_newVariables;
         } else {
            return getArgumentSize() + 2 + m_newVariables;
         }
      }

      public int indexOfResult() {
         return getArgumentSize() + 2;
      }

      public int indexOfTransaction() {
         return getArgumentSize() + 1;
      }

      public void loadEvent() {
         m_mv.visitVarInsn(ALOAD, indexOfEvent());
      }

      public void loadException() {
         m_mv.visitVarInsn(ALOAD, indexOfException());
      }

      public void loadResultIfHave() {
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

      public void storeEvent() {
         m_mv.visitVarInsn(ASTORE, indexOfEvent());
      }

      public void storeException() {
         m_mv.visitVarInsn(ASTORE, indexOfException());
      }

      public void storeResultIfHave() {
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

      public void storeTransaction() {
         m_mv.visitVarInsn(ASTORE, indexOfTransaction());
      }
   }

   private static class MethodWrapperForEvent extends MethodVisitor implements Opcodes {
      private Context m_ctx;

      private boolean m_return;

      private int m_maxStack;

      private int m_maxLocals;

      public MethodWrapperForEvent(Context ctx, MethodVisitor mv) {
         super(Opcodes.ASM5, mv);

         m_ctx = ctx;
      }

      private void buildBeforeReturnClause() {
         if (!m_return) {
            m_return = true;

            LocalVariables lvs = m_ctx.getLocalVariables();

            lvs.storeResultIfHave();
            m_ctx.eventIfHave();
            lvs.loadResultIfHave();
         }
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         Type type = Type.getType(desc);

         if (CatEvent.class.getName().equals(type.getClassName())) {
            return null; // delete it
         }

         return super.visitAnnotation(desc, visible);
      }

      @Override
      public void visitEnd() {
         buildBeforeReturnClause();

         super.visitMaxs(m_maxStack + 100, m_maxLocals + 100);
         super.visitEnd();
      }

      @Override
      public void visitInsn(int opcode) {
         if (opcode >= IRETURN && opcode <= RETURN) {
            buildBeforeReturnClause();
         }

         super.visitInsn(opcode);
      }

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
         m_maxStack = maxStack;
         m_maxLocals = maxLocals + m_ctx.getLocalVariables().getNewVariables();
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
         if (!m_return) {
            m_return = true;

            LocalVariables lvs = m_ctx.getLocalVariables();

            lvs.storeResultIfHave();

            m_ctx.transactionAddData();
            m_ctx.transactionSuccess();
            m_ctx.eventIfHave();
            mv.visitLabel(m_bizEnd);

            m_ctx.transactionComplete();
            lvs.loadResultIfHave();
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

         m_ctx.transactionStart();
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
               m_ctx.transactionSetStatus();
               m_ctx.throwException();
               index++;
            }
         }
      }

      private void buildErrorClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();

         mv.visitLabel(m_errorHandler);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Error" });
         lvs.storeException();
         m_ctx.transactionSetStatus();
         m_ctx.throwException();
      }

      private void buildFinallyClause() {
         LocalVariables lvs = m_ctx.getLocalVariables();
         int next = lvs.indexOfNext();

         mv.visitLabel(m_finallyHandler);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Throwable" });
         mv.visitVarInsn(ASTORE, next);
         m_ctx.transactionComplete();
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
         m_ctx.transactionSetStatus();
         m_ctx.throwException();
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         Type type = Type.getType(desc);

         if (CatTransaction.class.getName().equals(type.getClassName())) {
            return null; // delete it
         } else if (CatEvent.class.getName().equals(type.getClassName())) {
            return null; // delete it
         }

         return super.visitAnnotation(desc, visible);
      }

      @Override
      public void visitCode() {
         super.visitCode();
         buildBeforeTryClause();
      }

      @Override
      public void visitEnd() {
         buildBeforeReturnClause();

         buildAfterReturnClause();
         super.visitMaxs(m_maxStack + 100, m_maxLocals + 100);
         super.visitEnd();
      }

      @Override
      public void visitInsn(int opcode) {
         if (opcode >= IRETURN && opcode <= RETURN) {
            buildBeforeReturnClause();
         }

         super.visitInsn(opcode);
      }

      @Override
      public void visitMaxs(int maxStack, int maxLocals) {
         m_maxStack = maxStack;
         m_maxLocals = maxLocals + m_ctx.getLocalVariables().getNewVariables();
      }
   }
}
