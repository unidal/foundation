package org.unidal.lookup;

import java.util.List;
import java.util.Map;

import org.unidal.lookup.extension.Contextualizable;

public abstract class ContainerHolder implements Contextualizable {
   private PlexusContainer m_container;

   public void contextualize(Map<String, Object> context) {
      m_container = (PlexusContainer) context.get("plexus");
   }

   protected PlexusContainer getContainer() {
      return m_container;
   }

   protected <T> boolean hasComponent(Class<T> role) {
      return hasComponent(role, null);
   }

   protected <T> boolean hasComponent(Class<T> role, String roleHint) {
      return getContainer().hasComponent(role, roleHint);
   }

   protected <T> T lookup(Class<T> role) throws ComponentLookupException {
      return lookup(role, null);
   }

   protected <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException {
      return (T) getContainer().lookup(role, roleHint == null ? "default" : roleHint);
   }

   protected <T> List<T> lookupList(Class<T> role) throws ComponentLookupException {
      return (List<T>) getContainer().lookupList(role);
   }

   protected <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException {
      return (Map<String, T>) getContainer().lookupMap(role);
   }

   protected void release(Object component) throws ComponentLookupException {
      if (component != null) {
         getContainer().release(component);
      }
   }
}
