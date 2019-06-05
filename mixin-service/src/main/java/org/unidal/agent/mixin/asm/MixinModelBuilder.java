package org.unidal.agent.mixin.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.unidal.agent.mixin.MixinMeta;
import org.unidal.agent.mixin.MixinResourceProvider;
import org.unidal.agent.mixin.model.entity.ClassModel;
import org.unidal.agent.mixin.model.entity.FieldModel;
import org.unidal.agent.mixin.model.entity.InnerClassModel;
import org.unidal.agent.mixin.model.entity.MethodModel;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.entity.SourceModel;
import org.unidal.agent.mixin.model.entity.TargetModel;

public class MixinModelBuilder {
   private MixinResourceProvider m_provider;

   public MixinModelBuilder(MixinResourceProvider provider) {
      m_provider = provider;
   }

   public MixinModel build() {
      MixinModel mixin = new MixinModel();

      // step 1: get classes from resource provider, i.e. class loader
      Map<String, Boolean> m_classes = m_provider.getClasses("META-INF/mixin.properties");

      // step 2: build source for all mix-in classes
      List<Entry<String, Boolean>> classes = new ArrayList<Map.Entry<String, Boolean>>(m_classes.entrySet());

      Collections.reverse(classes);

      for (Map.Entry<String, Boolean> e : classes) {
         if (e.getValue().booleanValue()) { // open
            try {
               buildSource(mixin, e.getKey());
            } catch (Throwable t) {
               t.printStackTrace();
            }
         }
      }

      // step 3: build target for all source of the mix-in classes
      for (ClassModel classModel : mixin.getClasses().values()) {
         try {
            new TargetModelBuilder().build(classModel);
         } catch (Throwable t) {
            String message = String.format("Error when building mixin model for class(%s)!", classModel.getClazz());

            new RuntimeException(message, t).printStackTrace();
         }
      }

      return mixin;
   }

   private void buildSource(MixinModel mixin, String name) throws IOException {
      SourceModelBuilder builder = new SourceModelBuilder();
      SourceModel sourceModel = builder.build(name);
      String className = builder.getClassName();
      ClassModel classModel = mixin.findOrCreateClass(className);

      classModel.addSource(sourceModel);
   }

   private static class InnerClassBuilder extends ClassVisitor {
      private SourceModel m_sourceModel;

      private String m_outerName;

      public InnerClassBuilder(SourceModel sourceModel, String name) {
         super(Opcodes.ASM5);

         m_sourceModel = sourceModel;
         m_outerName = name;
      }

      public void build() {
         try {
            ClassReader reader = new ClassReader(m_outerName);
            int flags = ClassReader.SKIP_FRAMES + ClassReader.SKIP_DEBUG + ClassReader.SKIP_CODE;

            reader.accept(this, flags);
         } catch (Throwable e) {
            e.printStackTrace();
         }
      }

      @Override
      public void visitInnerClass(String name, String outerName, String innerName, int access) {
         if (!m_outerName.equals(name)) {
            InnerClassModel innerClassModel = m_sourceModel.findInnerClass(name);

            if (innerClassModel == null) {
               innerClassModel = m_sourceModel.findOrCreateInnerClass(name);
               innerClassModel.setAccess(access);
               innerClassModel.setInnerName(innerName);
               innerClassModel.setOuterName(outerName);

               new InnerClassBuilder(m_sourceModel, name).build();
            }
         }
      }
   }

   private static class MixinMetaVisitor extends AnnotationVisitor {
      private AtomicReference<String> m_classNameRef;

      public MixinMetaVisitor(AtomicReference<String> classNameRef) {
         super(Opcodes.ASM5);

         m_classNameRef = classNameRef;
      }

      @Override
      public void visit(String name, Object value) {
         if ("targetClass".equals(name)) {
            Type type = (Type) value;

            m_classNameRef.set(type.getClassName());
         } else if ("targetClassName".equals(name)) {
            m_classNameRef.set((String) value);
         }
      }
   }

   private static class SourceModelBuilder extends ClassVisitor {
      private SourceModel m_sourceModel;

      private AtomicReference<String> m_classNameRef = new AtomicReference<String>();

      public SourceModelBuilder() {
         super(Opcodes.ASM5);
      }

      public SourceModel build(String name) throws IOException {
         ClassReader reader = new ClassReader(name);
         int flags = ClassReader.SKIP_FRAMES + ClassReader.SKIP_DEBUG + ClassReader.SKIP_CODE;

         m_sourceModel = new SourceModel(reader.getClassName());
         reader.accept(this, flags);
         return m_sourceModel;
      }

      public String getClassName() {
         return m_classNameRef.get();
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         Type type = Type.getType(desc);

         if (MixinMeta.class.getName().equals(type.getClassName())) {
            return new MixinMetaVisitor(m_classNameRef);
         }

         return null;
      }

      @Override
      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
         FieldModel field = new FieldModel();

         field.setAccess(access);
         field.setName(name);
         field.setDesc(desc);

         m_sourceModel.addField(field);
         return null;
      }

      @Override
      public void visitInnerClass(String name, String outerName, String innerName, int access) {
         if (m_sourceModel.getName().equals(outerName)) {
            InnerClassModel innerClassModel = m_sourceModel.findOrCreateInnerClass(name);

            innerClassModel.setAccess(access);
            innerClassModel.setInnerName(innerName);
            innerClassModel.setOuterName(outerName);

            new InnerClassBuilder(m_sourceModel, name).build();
         }
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         if (name.startsWith("$_")) {
            return null;
         }

         MethodModel method = new MethodModel();

         method.setAccess(access);
         method.setName(name);
         method.setDesc(desc);

         m_sourceModel.addMethod(method);
         return null;
      }

      @Override
      public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
         return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
      }
   }

   private static class TargetModelBuilder extends ClassVisitor {
      private TargetModel m_targetModel;

      public TargetModelBuilder() {
         super(Opcodes.ASM5);
      }

      public void build(ClassModel classModel) throws IOException {
         String className = classModel.getClazz().replace('.', '/');
         ClassReader reader = readClass(className);
         int flags = ClassReader.SKIP_FRAMES + ClassReader.SKIP_DEBUG + ClassReader.SKIP_CODE;

         m_targetModel = new TargetModel(className);
         reader.accept(this, flags);
         classModel.setTarget(m_targetModel);
      }

      private ClassReader readClass(String className) throws IOException {
         try {
            return new ClassReader(className);
         } catch (IOException e) {
            InputStream in = getClass().getResourceAsStream("/" + className + ".class");

            if (in == null) {
               in = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + className + ".class");
            }

            return new ClassReader(in);
         }
      }

      @Override
      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
         FieldModel field = new FieldModel();

         field.setAccess(access);
         field.setName(name);
         field.setDesc(desc);

         m_targetModel.addField(field);
         return null;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         MethodModel method = new MethodModel();

         method.setAccess(access);
         method.setName(name);
         method.setDesc(desc);

         m_targetModel.addMethod(method);
         return null;
      }

      @Override
      public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
         return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
      }
   }
}
