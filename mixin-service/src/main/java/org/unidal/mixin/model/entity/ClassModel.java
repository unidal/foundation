/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.mixin.model.entity;

import static org.unidal.mixin.model.Constants.ATTR_CLASS;
import static org.unidal.mixin.model.Constants.ENTITY_CLASS;

import java.util.ArrayList;
import java.util.List;

import org.unidal.mixin.model.BaseEntity;
import org.unidal.mixin.model.IVisitor;

public class ClassModel extends BaseEntity<ClassModel> {
   private String m_class;

   private TargetModel m_target;

   private List<SourceModel> m_sources = new ArrayList<SourceModel>();

   public ClassModel() {
   }

   public ClassModel(String _class) {
      m_class = _class;
   }

   @Override
   public void accept(IVisitor visitor) {
      visitor.visitClass(this);
   }

   public ClassModel addSource(SourceModel source) {
      m_sources.add(source);
      return this;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof ClassModel) {
         ClassModel _o = (ClassModel) obj;

         if (!equals(getClazz(), _o.getClazz())) {
            return false;
         }

         return true;
      }

      return false;
   }

   public SourceModel findSource(String name) {
      for (SourceModel source : m_sources) {
         if (!equals(source.getName(), name)) {
            continue;
         }

         return source;
      }

      return null;
   }

   public SourceModel findOrCreateSource(String name) {
      synchronized (m_sources) {
         for (SourceModel source : m_sources) {
            if (!equals(source.getName(), name)) {
               continue;
            }

            return source;
         }

         SourceModel source = new SourceModel(name);

         m_sources.add(source);
         return source;
      }
   }

   public String getClazz() {
      return m_class;
   }

   public List<SourceModel> getSources() {
      return m_sources;
   }

   public TargetModel getTarget() {
      return m_target;
   }

   @Override
   public int hashCode() {
      int hash = 0;

      hash = hash * 31 + (m_class == null ? 0 : m_class.hashCode());

      return hash;
   }

   @Override
   public void mergeAttributes(ClassModel other) {
      assertAttributeEquals(other, ENTITY_CLASS, ATTR_CLASS, m_class, other.getClazz());

   }

   public SourceModel removeSource(String name) {
      int len = m_sources.size();

      for (int i = 0; i < len; i++) {
         SourceModel source = m_sources.get(i);

         if (!equals(source.getName(), name)) {
            continue;
         }

         return m_sources.remove(i);
      }

      return null;
   }

   public ClassModel setClazz(String _class) {
      m_class = _class;
      return this;
   }

   public ClassModel setTarget(TargetModel target) {
      m_target = target;
      return this;
   }

}