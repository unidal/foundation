package org.unidal.agent.cat.sample.hello;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class HelloServiceDump implements Opcodes {

   public static byte[] dump() throws Exception {

      ClassWriter cw = new ClassWriter(0);
      FieldVisitor fv;
      MethodVisitor mv;
      AnnotationVisitor av0;

      cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "org/unidal/agent/cat/sample/hello/HelloService", null, "java/lang/Object",
            null);

      {
         av0 = cw.visitAnnotation("Lorg/unidal/agent/cat/CatEnabled;", true);
         av0.visit("value", Boolean.TRUE);
         av0.visitEnd();
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
         mv = cw.visitMethod(ACC_PUBLIC, "hello", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
         {
            av0 = mv.visitAnnotation("Lorg/unidal/agent/cat/CatTransaction;", true);
            av0.visit("type", "Service");
            av0.visit("name", "hello");
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
         {
            av0 = mv.visitAnnotation("Lorg/unidal/agent/cat/CatEvent;", true);
            av0.visit("type", "Guest");
            av0.visit("name", "${arg0}");
            av0.visitEnd();
         }
         mv.visitCode();
         
         mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
         mv.visitInsn(DUP);
         mv.visitLdcInsn("Hello ");
         mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
         mv.visitVarInsn(ALOAD, 1);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
               "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
         mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
         
         mv.visitInsn(ARETURN);
         mv.visitMaxs(3, 2);
         mv.visitEnd();
      }
      cw.visitEnd();

      return cw.toByteArray();
   }
}
