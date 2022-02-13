package org.unidal.lookup.container;

import java.util.List;

import org.unidal.lookup.ComponentLookupException;

public interface ComponentFactory {

   /**
    * Returns true if this container has a component with the given role/role-hint.
    * 
    * @param role
    *           the type of the component
    * @param roleHint
    *           a role hint to differentiate the implementation of component
    * @return true if this container has the component with the given role/role-hint
    */
   boolean hasComponent(String role, String roleHint);

   /**
    * Looks up the component with the given role/role-hint.
    * 
    * @param role
    *           the type of the component
    * @param roleHint
    *           a role hint to differentiate the implementation of component
    * @return an instance of the component
    * @throws ComponentLookupException
    *            when error occurs
    */
   Object lookup(String role, String roleHint) throws ComponentLookupException;

   /**
    * Returns a list of role hints of the given component.
    * 
    * @param role
    *           the type of the component
    * @return list of the component role hints, null otherwise
    */
   public List<String> getRoleHints(String role);
}
