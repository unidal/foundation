package org.unidal.lookup.container;

import java.util.List;

import org.unidal.lookup.ComponentLookupException;

public interface ComponentFactory {

   /**
    * Returns true if this container has a component with the given role/role-hint.
    * 
    * @param role
    *           the non-unique type of the component
    * @param roleHint
    *           a hint for the desired component implementation
    * @return true if this container has a component with the given role/role-hint
    */
   boolean hasComponent(String role, String roleHint);

   /**
    * Looks up and returns a component object with the given unique role/role-hint combination.
    * 
    * @param role
    *           the non-unique type of the component
    * @param roleHint
    *           a hint for the desired component implementation
    * @return an instance of component object
    * @throws ComponentLookupException
    *            when error
    */
   Object lookup(String role, String roleHint) throws ComponentLookupException;

   /**
    * Returns available list of component role hints.
    * 
    * @param role
    *           the non-unique type of the component
    * @return list of component role hints
    */
   public List<String> getRoleHints(String role);
}
