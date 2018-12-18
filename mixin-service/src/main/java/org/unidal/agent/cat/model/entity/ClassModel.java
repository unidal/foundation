/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.agent.cat.model.entity;

import static org.unidal.agent.cat.model.Constants.ATTR_NAME;
import static org.unidal.agent.cat.model.Constants.ENTITY_CLASS;

import java.util.ArrayList;
import java.util.List;

import org.unidal.agent.cat.model.BaseEntity;
import org.unidal.agent.cat.model.IVisitor;

public class ClassModel extends BaseEntity<ClassModel> {
   private String m_name;

   private Boolean m_enabled;

   private List<MethodModel> m_methods = new ArrayList<MethodModel>();

   public ClassModel() {
   }

   public ClassModel(String name) {
      m_name = name;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitClass(this);
   }

   public ClassModel addMethod(MethodModel method) {
      m_methods.add(method);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ClassModel) {
         ClassModel _o = (ClassModel) obj;

         if (!equals(getName(), _o.getName())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public MethodModel findMethod(String name, String desc) {
      for (MethodModel method : m_methods) {
         if (!equals(method.getName(), name)) {
            continue;
         }

         if (!equals(method.getDesc(), desc)) {
            continue;
         }

         return method;
      }

      return null;
   }

   public MethodModel findOrCreateMethod(String name, String desc) {
      synchronized (m_methods) {
         for (MethodModel method : m_methods) {
            if (!equals(method.getName(), name)) {
               continue;
            }

            if (!equals(method.getDesc(), desc)) {
               continue;
            }

            return method;
         }

         MethodModel method = new MethodModel(name, desc);

         m_methods.add(method);
         return method;
      }
   }

   public Boolean getEnabled() {
      return m_enabled;
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

   public boolean isEnabled() {
      return m_enabled != null && m_enabled.booleanValue();
   }

   @Override
   public void mergeAttributes(ClassModel other) {
      assertAttributeEquals(other, ENTITY_CLASS, ATTR_NAME, m_name, other.getName());

      if (other.getEnabled() != null) {
         m_enabled = other.getEnabled();
      }
   }

   public MethodModel removeMethod(String name, String desc) {
      int len = m_methods.size();

      for (int i = 0; i < len; i++) {
         MethodModel method = m_methods.get(i);

         if (!equals(method.getName(), name)) {
            continue;
         }

         if (!equals(method.getDesc(), desc)) {
            continue;
         }

         return m_methods.remove(i);
      }

      return null;
   }

   public ClassModel setEnabled(Boolean enabled) {
      m_enabled = enabled;
      return this;
   }

   public ClassModel setName(String name) {
      m_name = name;
      return this;
   }

}
