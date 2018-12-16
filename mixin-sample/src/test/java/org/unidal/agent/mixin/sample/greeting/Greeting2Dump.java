package org.unidal.agent.mixin.sample.greeting;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Greeting2Dump implements Opcodes {
   public static byte[] dump() throws Exception {
      ClassWriter cw = new ClassWriter(0);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;

      cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "org/unidal/mixin/sample/greeting/Greeting2", null, "java/lang/Object",
            null);

      {
         av0 = cw.visitAnnotation("Lorg/unidal/mixin/cat/CatEnabled;", true);
         av0.visitEnd();
      }
      {
         fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "DEBUG", "Z", null, null);
         fv.visitEnd();
      }
      {
         mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
         mv.visitCode();
         mv.visitInsn(ICONST_1);
         mv.visitFieldInsn(PUTSTATIC, "org/unidal/mixin/sample/greeting/Greeting2", "DEBUG", "Z");
         mv.visitInsn(RETURN);
         mv.visitMaxs(1, 0);
         mv.visitEnd();
      }
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
         mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
               new String[] { "java/lang/Exception" });
         mv.visitCode();
         mv.visitInsn(ICONST_1);
         mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
         mv.visitInsn(DUP);
         mv.visitInsn(ICONST_0);
         mv.visitLdcInsn(Type.getType("Lorg/unidal/mixin/sample/greeting/Greeting2;"));
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false);
         mv.visitInsn(AASTORE);
         mv.visitMethodInsn(INVOKESTATIC, "org/objectweb/asm/util/ASMifier", "main", "([Ljava/lang/String;)V", false);
         mv.visitInsn(RETURN);
         mv.visitMaxs(4, 1);
         mv.visitEnd();
      }
      {
         mv = cw.visitMethod(ACC_PUBLIC, "sayHello", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
         {
            av0 = mv.visitAnnotation("Lorg/unidal/mixin/cat/CatTransaction;", true);
            av0.visit("type", "${class}");
            av0.visit("name", "${method}");
            av0.visit("status", "${return}:200,201");
            {
               AnnotationVisitor av1 = av0.visitArray("keys");
               av1.visit(null, "name");
               av1.visit(null, "return");
               av1.visitEnd();
            }
            {
               AnnotationVisitor av1 = av0.visitArray("values");
               av1.visit(null, "${arg0}");
               av1.visit(null, "${return}");
               av1.visitEnd();
            }
            av0.visitEnd();
         }
         mv.visitCode();
         mv.visitFieldInsn(GETSTATIC, "org/unidal/mixin/sample/greeting/Greeting2", "DEBUG", "Z");
         Label l0 = new Label();
         mv.visitJumpInsn(IFEQ, l0);
         mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
         mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
         mv.visitInsn(DUP);
         mv.visitLdcInsn("[");
         mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
         mv.visitVarInsn(ALOAD, 0);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitLdcInsn("] sayHello: ");
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
         mv.visitLabel(l0);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
         mv.visitInsn(DUP);
         mv.visitLdcInsn("Hello ");
         mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
         mv.visitInsn(ARETURN);
         mv.visitMaxs(4, 2);
         mv.visitEnd();
      }
      {
         mv = cw.visitMethod(ACC_PUBLIC, "sayHello2", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
         mv.visitCode();
         Label l0 = new Label();
         Label l1 = new Label();
         Label l2 = new Label();
         mv.visitTryCatchBlock(l0, l1, l2, "java/lang/RuntimeException");
         Label l3 = new Label();
         mv.visitTryCatchBlock(l0, l1, l3, "java/lang/Error");
         Label l4 = new Label();
         mv.visitTryCatchBlock(l0, l1, l4, null);
         mv.visitTryCatchBlock(l2, l4, l4, null);
         mv.visitLdcInsn("Greeting2");
         mv.visitLdcInsn("sayHello");
         mv.visitMethodInsn(INVOKESTATIC, "org/unidal/cat/Cat", "newTransaction",
               "(Ljava/lang/String;Ljava/lang/String;)Lorg/unidal/cat/message/Transaction;", false);
         mv.visitVarInsn(ASTORE, 2);
         mv.visitLabel(l0);
         mv.visitFieldInsn(GETSTATIC, "org/unidal/mixin/sample/greeting/Greeting2", "DEBUG", "Z");
         Label l5 = new Label();
         mv.visitJumpInsn(IFEQ, l5);
         mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
         mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
         mv.visitInsn(DUP);
         mv.visitLdcInsn("[");
         mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
         mv.visitVarInsn(ALOAD, 0);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitLdcInsn("] sayHello: ");
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
         mv.visitLabel(l5);
         mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "org/unidal/cat/message/Transaction" }, 0, null);
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
         Label l6 = new Label();
         mv.visitJumpInsn(IFNE, l6);
         mv.visitLdcInsn("201");
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
         Label l7 = new Label();
         mv.visitJumpInsn(IFEQ, l7);
         mv.visitLabel(l6);
         mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "java/lang/String" }, 0, null);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "success", "()V", true);
         Label l8 = new Label();
         mv.visitJumpInsn(GOTO, l8);
         mv.visitLabel(l7);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "setStatus", "(Ljava/lang/String;)V",
               true);
         mv.visitLabel(l8);
         mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
         mv.visitVarInsn(ALOAD, 3);
         mv.visitVarInsn(ASTORE, 5);
         mv.visitLabel(l1);
         mv.visitVarInsn(ALOAD, 2);
         mv.visitMethodInsn(INVOKEINTERFACE, "org/unidal/cat/message/Transaction", "complete", "()V", true);
         mv.visitVarInsn(ALOAD, 5);
         mv.visitInsn(ARETURN);
         mv.visitLabel(l2);
         mv.visitFrame(Opcodes.F_FULL, 3, new Object[] { "org/unidal/mixin/sample/greeting/Greeting2",
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
         mv.visitMaxs(4, 6);
         mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
   }
}
