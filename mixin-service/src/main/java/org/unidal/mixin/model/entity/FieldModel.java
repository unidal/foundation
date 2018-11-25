/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.mixin.model.entity;

import static org.unidal.mixin.model.Constants.ATTR_NAME;
import static org.unidal.mixin.model.Constants.ENTITY_FIELD;

import org.unidal.mixin.model.BaseEntity;
import org.unidal.mixin.model.IVisitor;

public class FieldModel extends BaseEntity<FieldModel> {
   private Integer m_access;

   private String m_name;

   private String m_desc;

   private String m_sourceName;

   private String m_sourceClass;

   public FieldModel() {
   }

   public FieldModel(String name) {
      m_name = name;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitField(this);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof FieldModel) {
         FieldModel _o = (FieldModel) obj;

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

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_name == null ? 0 : m_name.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(FieldModel other) {
      assertAttributeEquals(other, ENTITY_FIELD, ATTR_NAME, m_name, other.getName());

      if (other.getAccess() != null) {
         m_access = other.getAccess();
      }

      if (other.getDesc() != null) {
         m_desc = other.getDesc();
      }

      if (other.getSourceName() != null) {
         m_sourceName = other.getSourceName();
      }

      if (other.getSourceClass() != null) {
         m_sourceClass = other.getSourceClass();
      }
   }

   public FieldModel setAccess(Integer access) {
      m_access = access;
      return this;
   }

   public FieldModel setDesc(String desc) {
      m_desc = desc;
      return this;
   }

   public FieldModel setName(String name) {
      m_name = name;
      return this;
   }

   public FieldModel setSourceClass(String sourceClass) {
      m_sourceClass = sourceClass;
      return this;
   }

   public FieldModel setSourceName(String sourceName) {
      m_sourceName = sourceName;
      return this;
   }

}
