package org.unidal.mixin.asm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.unidal.mixin.model.entity.ClassModel;
import org.unidal.mixin.model.entity.FieldModel;
import org.unidal.mixin.model.entity.InnerClassModel;
import org.unidal.mixin.model.entity.MethodModel;
import org.unidal.mixin.model.entity.MixinModel;
import org.unidal.mixin.model.entity.SourceModel;
import org.unidal.mixin.model.entity.TargetModel;
import org.unidal.mixin.model.transform.BaseVisitor;

public class MixinModelAggregator extends BaseVisitor {
   private MixinModel m_mixin;

   private ClassModel m_classModel;

   private TargetModel m_targetModel;

   private SourceModel m_source;

   private TargetModel m_target;

   private Map<String, List<MethodEntry>> m_methodMap = new LinkedHashMap<String, List<MethodEntry>>();

   private Map<String, List<FieldEntry>> m_fieldMap = new LinkedHashMap<String, List<FieldEntry>>();

   public MixinModel aggregate(MixinModel model) {
      m_mixin = new MixinModel();

      model.accept(this);
      return m_mixin;
   }

   @Override
   public void visitClass(ClassModel _class) {
      m_classModel = m_mixin.findOrCreateClass(_class.getClazz());
      m_targetModel = new TargetModel(_class.getClazz().replace('.', '/'));
      m_classModel.setTarget(m_targetModel);

      for (SourceModel source : _class.getSources()) {
         visitSource(source);
      }

      visitTarget(_class.getTarget());
   }

   @Override
   public void visitField(FieldModel field) {
      // build the map
      List<FieldEntry> entries = m_fieldMap.get(field.getName());

      if (entries == null) {
         entries = new ArrayList<FieldEntry>();
         m_fieldMap.put(field.getName(), entries);
      }

      FieldEntry entry = new FieldEntry(field);

      entry.setIndex(entries.size());
      entry.setSourceClass(m_source != null ? m_source.getName() : m_target.getName());
      entries.add(entry);

      if (m_target != null) {
         for (FieldEntry e : entries) {
            String name = e.getName();
            FieldModel f = m_target.findOrCreateField(name);

            f.setAccess(e.getAccess());
            f.setDesc(field.getDesc());
            f.setSourceName(field.getName());
            f.setSourceClass(e.getSourceClass());
         }
      } else if (m_source != null) {
         FieldModel f = m_classModel.getTarget().findOrCreateField(field.getName());
         String desc = field.getDesc();

         f.mergeAttributes(field);
         f.setSourceClass(m_source.getName());
         f.setSourceName(f.getName());

         // for inner class
         int off = desc.indexOf(m_source.getName());

         if (off == 1) {
            if (desc.charAt(off + m_source.getName().length()) == '$') {
               String target = m_classModel.getTarget().getName();

               f.setDesc(desc.substring(0, off) + target + desc.substring(m_source.getName().length() + 1));
            }
         }
      }

      if (m_source != null) { // copy
         FieldModel f = m_source.findOrCreateField(field.getName());

         f.mergeAttributes(field);
      }

      super.visitField(field);
   }

   @Override
   public void visitInnerClass(InnerClassModel innerClass) {
      TargetModel target = m_classModel.getTarget();
      InnerClassModel icm = target.findOrCreateInnerClass(innerClass.getName());

      icm.mergeAttributes(innerClass);

      if (m_target != null) {
         icm.setName(innerClass.getName());
         icm.setOuterName(innerClass.getOuterName());
         icm.setSourceName(innerClass.getName());
         icm.setSourceOuterName(innerClass.getOuterName());
      } else {
         icm.setName(innerClass.getName().replace(m_source.getName(), target.getName()));
         icm.setOuterName(innerClass.getOuterName().replace(m_source.getName(), target.getName()));
         icm.setSourceName(innerClass.getName());
         icm.setSourceOuterName(innerClass.getOuterName());
      }

      if (m_source != null) { // copy
         InnerClassModel ic = m_source.findOrCreateInnerClass(innerClass.getName());

         ic.mergeAttributes(innerClass);
      }

      super.visitInnerClass(innerClass);
   }

   @Override
   public void visitMethod(MethodModel method) {
      // build the map
      String key = method.getName() + method.getDesc();
      List<MethodEntry> entries = m_methodMap.get(key);

      if (entries == null) {
         entries = new ArrayList<MethodEntry>();
         m_methodMap.put(key, entries);
      }

      MethodEntry entry = new MethodEntry(method);

      entry.setIndex(entries.size());
      entry.setSourceClass(m_source != null ? m_source.getName() : m_target.getName());
      entries.add(entry);

      if (m_target != null) {
         for (MethodEntry e : entries) {
            String name = e.getName();
            MethodModel m = m_target.findOrCreateMethod(name, method.getDesc());

            m.setAccess(e.getAccess());
            m.setSuperName(e.getSuperName());
            m.setSourceName(method.getName());
            m.setSourceClass(e.getSourceClass());
         }

         m_methodMap.remove(key);
      }

      // copy the source
      if (m_source != null) {
         MethodModel m = m_source.findOrCreateMethod(method.getName(), method.getDesc());

         m.mergeAttributes(method);
      }

      super.visitMethod(method);
   }

   @Override
   public void visitSource(SourceModel source) {
      m_target = null;
      m_source = m_classModel.findOrCreateSource(source.getName());
      m_source.mergeAttributes(source);

      super.visitSource(source);
   }

   @Override
   public void visitTarget(TargetModel target) {
      m_source = null;
      m_target = m_classModel.getTarget();
      m_target.mergeAttributes(target);

      super.visitTarget(target);

      // for all private methods of source class
      for (List<MethodEntry> entries : m_methodMap.values()) {
         for (MethodEntry e : entries) {
            String name = e.getName();
            MethodModel m = m_target.findOrCreateMethod(name);

            m.setAccess(e.getAccess());
            m.setDesc(e.getMethod().getDesc());
            m.setSourceName(e.getMethod().getName());
            m.setSourceClass(e.getSourceClass());
         }
      }
   }

   private static class AccessHelper {
      private int m_access;

      public AccessHelper(int access) {
         m_access = access;
      }

      public int getValue() {
         return m_access;
      }

      public AccessHelper withPrivate() {
         if ((m_access & Opcodes.ACC_PUBLIC) != 0) {
            m_access ^= Opcodes.ACC_PUBLIC;
         }

         if ((m_access & Opcodes.ACC_PROTECTED) != 0) {
            m_access ^= Opcodes.ACC_PROTECTED;
         }

         m_access |= Opcodes.ACC_PRIVATE;
         return this;
      }

      public AccessHelper withSynthetic() {
         m_access |= Opcodes.ACC_SYNTHETIC;
         return this;
      }
   }

   private static class FieldEntry {
      private FieldModel m_field;

      private int m_index;

      private String m_sourceClass;

      public FieldEntry(FieldModel field) {
         m_field = field;
      }

      public int getAccess() {
         Integer access = m_field.getAccess();

         if (m_index == 0) {
            return access;
         } else {
            return new AccessHelper(access).withPrivate().withSynthetic().getValue();
         }
      }

      public String getName() {
         if (m_index == 0) {
            return m_field.getName();
         } else if (m_index == 1) {
            return "$_" + m_field.getName();
         } else {
            return "$" + m_index + "_" + m_field.getName();
         }
      }

      public String getSourceClass() {
         return m_sourceClass;
      }

      public void setIndex(int index) {
         m_index = index;
      }

      public void setSourceClass(String sourceClass) {
         m_sourceClass = sourceClass;
      }
   }

   private static class MethodEntry {
      private MethodModel m_method;

      private int m_index;

      private String m_sourceClass;

      public MethodEntry(MethodModel method) {
         m_method = method;
      }

      private String concat(Object... objs) {
         StringBuilder sb = new StringBuilder(64);

         for (Object obj : objs) {
            if (obj instanceof String) {
               String s = (String) obj;

               for (int i = 0; i < s.length(); i++) {
                  char ch = s.charAt(i);

                  if (ch == '<' || ch == '>') {
                     sb.append('$');
                  } else {
                     sb.append(ch);
                  }
               }
            } else {
               sb.append(obj);
            }
         }

         return sb.toString();
      }

      public int getAccess() {
         Integer access = m_method.getAccess();

         if (m_index == 0 || m_method.getName().startsWith("<")) {
            return access;
         } else {
            return new AccessHelper(access).withPrivate().withSynthetic().getValue();
         }
      }

      public MethodModel getMethod() {
         return m_method;
      }

      public String getName() {
         if ("<clinit>".equals(m_method.getName()) || "<init>".equals(m_method.getName())) {
            return concat("$", "_", m_method.getName(), "_", m_index);
         }

         if (m_index == 0) {
            return m_method.getName();
         } else if (m_index == 1) {
            return concat("$_", m_method.getName());
         } else {
            return concat("$", m_index, "_", m_method.getName());
         }
      }

      public String getSourceClass() {
         return m_sourceClass;
      }

      public String getSuperName() {
         if ("<clinit>".equals(m_method.getName()) || "<init>".equals(m_method.getName())) {
            return null;
         }

         if (m_index == 0) {
            return concat("$_", m_method.getName());
         } else {
            return concat("$", m_index + 1, "_", m_method.getName());
         }
      }

      public void setIndex(int index) {
         m_index = index;
      }

      public void setSourceClass(String sourceClass) {
         m_sourceClass = sourceClass;
      }

      @Override
      public String toString() {
         return m_method.toString();
      }
   }
}
