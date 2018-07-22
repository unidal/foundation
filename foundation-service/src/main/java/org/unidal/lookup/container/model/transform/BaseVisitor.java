/* THIS FILE WAS AUTO GENERATED BY codegen-maven-plugin, DO NOT EDIT IT */
package org.unidal.lookup.container.model.transform;

import org.unidal.lookup.container.model.IVisitor;
import org.unidal.lookup.container.model.entity.Any;
import org.unidal.lookup.container.model.entity.ComponentModel;
import org.unidal.lookup.container.model.entity.ConfigurationModel;
import org.unidal.lookup.container.model.entity.PlexusModel;
import org.unidal.lookup.container.model.entity.RequirementModel;

public abstract class BaseVisitor implements IVisitor {
   @Override
   public void visitAny(Any any) {
      for (Any child : any.getChildren()) {
         visitAny(child);
      }
   }

   @Override
   public void visitComponent(ComponentModel component) {
      if (component.getConfiguration() != null) {
         visitConfiguration(component.getConfiguration());
      }

      for (RequirementModel requirement : component.getRequirements()) {
         visitRequirement(requirement);
      }

      for (Any any : component.getDynamicElements()) {
         visitAny(any);
      }
   }

   @Override
   public void visitConfiguration(ConfigurationModel configuration) {
      for (Any any : configuration.getDynamicElements()) {
         visitAny(any);
      }
   }

   @Override
   public void visitPlexus(PlexusModel plexus) {
      for (ComponentModel component : plexus.getComponents()) {
         visitComponent(component);
      }
   }

   @Override
   public void visitRequirement(RequirementModel requirement) {
      for (Any any : requirement.getDynamicElements()) {
         visitAny(any);
      }
   }
}
