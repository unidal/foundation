/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.mixin.model.entity;

import static org.unidal.mixin.model.Constants.ATTR_NAME;
import static org.unidal.mixin.model.Constants.ENTITY_SOURCE;

import java.util.ArrayList;
import java.util.List;

import org.unidal.mixin.model.BaseEntity;
import org.unidal.mixin.model.IVisitor;

public class SourceModel extends BaseEntity<SourceModel> {
   private String m_name;

   private String m_class;

   private List<FieldModel> m_fields = new ArrayList<FieldModel>();

   private List<MethodModel> m_methods = new ArrayList<MethodModel>();

   private List<InnerClassModel> m_innerClasses = new ArrayList<InnerClassModel>();

   public SourceModel() {
   }

   public SourceModel(String name) {
      m_name = name;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitSource(this);
   }

   public SourceModel addField(FieldModel field) {
      m_fields.add(field);
      return this;
   }

   public SourceModel addInnerClass(InnerClassModel innerClass) {
      m_innerClasses.add(innerClass);
      return this;
   }

   public SourceModel addMethod(MethodModel method) {
      m_methods.add(method);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof SourceModel) {
         SourceModel _o = (SourceModel) obj;

         if (!equals(getName(), _o.getName())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public FieldModel findField(String name) {
      for (FieldModel field : m_fields) {
         if (!equals(field.getName(), name)) {
            continue;
         }

         return field;
      }

      return null;
   }

   public InnerClassModel findInnerClass(String innerName) {
      for (InnerClassModel innerClass : m_innerClasses) {
         if (!equals(innerClass.getInnerName(), innerName)) {
            continue;
         }

         return innerClass;
      }

      return null;
   }

   public MethodModel findMethod(String name) {
      for (MethodModel method : m_methods) {
         if (!equals(method.getName(), name)) {
            continue;
         }

         return method;
      }

      return null;
   }

   public FieldModel findOrCreateField(String name) {
      synchronized (m_fields) {
         for (FieldModel field : m_fields) {
            if (!equals(field.getName(), name)) {
               continue;
            }

            return field;
         }

         FieldModel field = new FieldModel(name);

         m_fields.add(field);
         return field;
      }
   }

   public InnerClassModel findOrCreateInnerClass(String innerName) {
      synchronized (m_innerClasses) {
         for (InnerClassModel innerClass : m_innerClasses) {
            if (!equals(innerClass.getInnerName(), innerName)) {
               continue;
            }

            return innerClass;
         }

         InnerClassModel innerClass = new InnerClassModel(innerName);

         m_innerClasses.add(innerClass);
         return innerClass;
      }
   }

   public MethodModel findOrCreateMethod(String name) {
      synchronized (m_methods) {
         for (MethodModel method : m_methods) {
            if (!equals(method.getName(), name)) {
               continue;
            }

            return method;
         }

         MethodModel method = new MethodModel(name);

         m_methods.add(method);
         return method;
      }
   }

   public String getClazz() {
      return m_class;
   }

   public List<FieldModel> getFields() {
      return m_fields;
   }

   public List<InnerClassModel> getInnerClasses() {
      return m_innerClasses;
   }

   public List<MethodModel> getMethods() {
      return m_methods;
   }

   public String getName() {
      return m_name;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(SourceModel other) {
      assertAttributeEquals(other, ENTITY_SOURCE, ATTR_NAME, m_name, other.getName());

      if (other.getClazz() != null) {
         m_class = other.getClazz();
      }
   }

   public FieldModel removeField(String name) {
      int len = m_fields.size();

      for (int i = 0; i < len; i++) {
         FieldModel field = m_fields.get(i);

         if (!equals(field.getName(), name)) {
            continue;
         }

         return m_fields.remove(i);
      }

      return null;
   }

   public InnerClassModel removeInnerClass(String innerName) {
      int len = m_innerClasses.size();

      for (int i = 0; i < len; i++) {
         InnerClassModel innerClass = m_innerClasses.get(i);

         if (!equals(innerClass.getInnerName(), innerName)) {
            continue;
         }

         return m_innerClasses.remove(i);
      }

      return null;
   }

   public MethodModel removeMethod(String name) {
      int len = m_methods.size();

      for (int i = 0; i < len; i++) {
         MethodModel method = m_methods.get(i);

         if (!equals(method.getName(), name)) {
            continue;
         }

         return m_methods.remove(i);
      }

      return null;
   }

   public SourceModel setClazz(String _class) {
      m_class = _class;
      return this;
   }

   public SourceModel setName(String name) {
      m_name = name;
      return this;
   }

}