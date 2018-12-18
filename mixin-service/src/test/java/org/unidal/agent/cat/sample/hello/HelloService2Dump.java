package org.unidal.agent.cat.sample.hello;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class HelloService2Dump implements Opcodes {

   public static byte[] dump() throws Exception {

      ClassWriter cw = new ClassWriter(0);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;

      cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "org/unidal/agent/cat/sample/hello/HelloService", null, "java/lang/Object",
            null);

      {
         mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
         mv.visitCode();
         mv.visitVarInsn(ALOAD, 0);
         mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
         mv.visitInsn(RETURN);
         mv.visitMaxs(1, 1);
         mv.visitEnd();
      }
      {
         mv = cw.visitMethod(ACC_PUBLIC, "hello", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
         mv.visitCode();
         
         Label l0 = new Label();
         Label l1 = new Label();
         Label l2 = new Label();
         Label l3 = new Label();
         Label l4 = new Label();

         mv.visitTryCatchBlock(l0, l1, l2, "java/lang/RuntimeException");
         mv.visitTryCatchBlock(l0, l1, l3, "java/lang/Error");
         mv.visitTryCatchBlock(l0, l1, l4, null);
         mv.visitTryCatchBlock(l2, l4, l4, null);
         
         mv.visitLdcInsn("Service");
         mv.visitLdcInsn("hello");
         mv.visitMethodInsn(INVOKESTATIC, "org/unidal/cat/Cat", "newTransaction",
               "(Ljava/lang/String;Ljava/lang/String;)Lorg/unidal/cat/message/Transaction;", false);
         mv.visitVarInsn(ASTORE, 2);
         mv.visitLabel(l0);
         
         mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
         mv.visitInsn(DUP);
         mv.visitLdcInsn("Hello ");
         mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
         
         mv.visitVarInsn(ASTORE, 3);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitLdcInsn("name");
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "addData",
               "(Ljava/lang/String;Ljava/lang/Object;)V", true);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitLdcInsn("return");
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "addData",
               "(Ljava/lang/String;Ljava/lang/Object;)V", true);
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
         mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "org/unidal/cat/message/Transaction", "java/lang/String" },
               0, null);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "success", "()V", true);
         Label l7 = new Label();
         mv.visitJumpInsn(GOTO, l7);
         mv.visitLabel(l6);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "setStatus", "(Ljava/lang/String;)V",
               true);
         mv.visitLabel(l7);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitLdcInsn("Guest");
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKESTATIC, "org/unidal/cat/Cat", "logEvent", "(Ljava/lang/String;Ljava/lang/String;)V",
               false);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitVarInsn(ASTORE, 5);
         mv.visitLabel(l1);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "complete", "()V", true);
         mv.visitVarInsn(ALOAD, 5);
         
         mv.visitInsn(ARETURN);
         
         mv.visitLabel(l2);
         mv.visitFrame(Opcodes.F_FULL, 3, new Object[] { "org/unidal/agent/cat/sample/hello/HelloService2",
               "java/lang/String", "org/unidal/cat/message/Transaction" }, 1,
               new Object[] { "java/lang/RuntimeException" });
         mv.visitVarInsn(ASTORE, 3);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "setStatus",
               "(Ljava/lang/Throwable;)V", true);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitInsn(ATHROW);
         
         mv.visitLabel(l3);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Error" });
         mv.visitVarInsn(ASTORE, 3);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "setStatus",
               "(Ljava/lang/Throwable;)V", true);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitInsn(ATHROW);
         
         mv.visitLabel(l4);
         mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Throwable" });
         mv.visitVarInsn(ASTORE, 4);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "complete", "()V", true);
         mv.visitVarInsn(ALOAD, 4);
         mv.visitInsn(ATHROW);
         
         mv.visitMaxs(3, 6);
         mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
   }
}
