/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.mixin.model.entity;

import static org.unidal.mixin.model.Constants.ATTR_NAME;
import static org.unidal.mixin.model.Constants.ENTITY_METHOD;

import org.unidal.mixin.model.BaseEntity;
import org.unidal.mixin.model.IVisitor;

public class MethodModel extends BaseEntity<MethodModel> {
   private Integer m_access;

   private String m_name;

   private String m_superName;

   private String m_sourceName;

   private String m_sourceClass;

   private String m_desc;

   public MethodModel() {
   }

   public MethodModel(String name) {
      m_name = name;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitMethod(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof MethodModel) {
         MethodModel _o = (MethodModel) obj;

         if (!equals(getName(), _o.getName())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public Integer getAccess() {
      return m_access;
   }

   public String getDesc() {
      return m_desc;
   }

   public String getName() {
      return m_name;
   }

   public String getSourceClass() {
      return m_sourceClass;
   }

   public String getSourceName() {
      return m_sourceName;
   }

   public String getSuperName() {
      return m_superName;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(MethodModel other) {
      assertAttributeEquals(other, ENTITY_METHOD, ATTR_NAME, m_name, other.getName());

      if (other.getAccess() != null) {
         m_access = other.getAccess();
      }

      if (other.getSuperName() != null) {
         m_superName = other.getSuperName();
      }

      if (other.getSourceName() != null) {
         m_sourceName = other.getSourceName();
      }

      if (other.getSourceClass() != null) {
         m_sourceClass = other.getSourceClass();
      }

      if (other.getDesc() != null) {
         m_desc = other.getDesc();
      }
   }

   public MethodModel setAccess(Integer access) {
      m_access = access;
      return this;
   }

   public MethodModel setDesc(String desc) {
      m_desc = desc;
      return this;
   }

   public MethodModel setName(String name) {
      m_name = name;
      return this;
   }

   public MethodModel setSourceClass(String sourceClass) {
      m_sourceClass = sourceClass;
      return this;
   }

   public MethodModel setSourceName(String sourceName) {
      m_sourceName = sourceName;
      return this;
   }

   public MethodModel setSuperName(String superName) {
      m_superName = superName;
      return this;
   }

}