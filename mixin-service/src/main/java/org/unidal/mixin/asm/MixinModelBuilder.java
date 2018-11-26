package org.unidal.mixin.asm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.unidal.helper.Splitters;
import org.unidal.mixin.MixinMeta;
import org.unidal.mixin.model.entity.ClassModel;
import org.unidal.mixin.model.entity.FieldModel;
import org.unidal.mixin.model.entity.InnerClassModel;
import org.unidal.mixin.model.entity.MethodModel;
import org.unidal.mixin.model.entity.MixinModel;
import org.unidal.mixin.model.entity.SourceModel;
import org.unidal.mixin.model.entity.TargetModel;

public class MixinModelBuilder {
   private Map<String, Boolean> m_classes = new LinkedHashMap<String, Boolean>();

   public MixinModel build() {
      MixinModel mixin = new MixinModel();
      List<URL> urls = getConfigurations();

      // step 1: collect mix-in classes from META-INF/mixin.properties in the class paths
      for (URL url : urls) {
         try {
            loadMixinClasses(url, m_classes);
         } catch (Throwable t) {
            t.printStackTrace();
         }
      }

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
            t.printStackTrace();
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

   private List<URL> getConfigurations() {
      List<URL> urls = new ArrayList<URL>();

      try {
         Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/mixin.properties");

         while (resources.hasMoreElements()) {
            urls.add(resources.nextElement());
         }
      } catch (Throwable e) {
         // ignore it
         e.printStackTrace();
      }

      return urls;
   }

   private void loadMixinClasses(URL url, Map<String, Boolean> classes) throws IOException {
      InputStream in = url.openStream();

      try {
         Properties properties = new Properties();

         properties.load(in);

         for (String name : properties.stringPropertyNames()) {
            List<String> items = Splitters.by(',').noEmptyItem().trim().split(name);

            for (String item : items) {
               if (item.startsWith("-")) {
                  classes.put(item.substring(1), false);
               } else {
                  Boolean open = classes.get(item);

                  if (open == null || open.booleanValue()) {
                     classes.put(item, true);
                  }
               }
            }
         }
      } finally {
         try {
            in.close();
         } catch (IOException e) {
            // ignore it
         }
      }
   }

   public void register(String mixinClass) {
      m_classes.put(mixinClass, true);
   }

   // for test case only
   public void unregister(String mixinClass) {
      m_classes.remove(mixinClass);
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
            InnerClassModel innerClassModel = m_sourceModel.findOrCreateInnerClass(innerName);

            innerClassModel.setAccess(access);
            innerClassModel.setName(name);
            innerClassModel.setOuterName(outerName);
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
         int flags = ClassReader.SKIP_FRAMES + ClassReader.SKIP_DEBUG + ClassReader.SKIP_CODE;

         m_targetModel = new TargetModel(className);
         new ClassReader(className).accept(this, flags);
         classModel.setTarget(m_targetModel);
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
         // if (name.equals("<clinit>") || name.equals("<init>")) {
         // return null;
         // }

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
