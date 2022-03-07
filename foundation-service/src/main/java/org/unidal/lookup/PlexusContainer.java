package org.unidal.lookup;

import java.util.List;
import java.util.Map;

import org.unidal.lookup.logging.Logger;

/**
 * PlexusContainer is the entry-point for loading and accessing other components.
 */
public interface PlexusContainer {
   // ------------------------------------------------------------------------
   // Lookup
   // ------------------------------------------------------------------------

   /**
    * Adds live component instance to this container.
    *
    * Component instance is not associated with any class realm and will be ignored during lookup is lookup realm is provided using
    * thread context classloader.
    * 
    * @param component
    *           component to be added
    * @param role
    *           role
    * @param roleHint
    *           role hint
    */
   <T> void addComponent(T component, Class<?> role, String roleHint);

   /**
    * Adds a component model to this container.
    * 
    * @param componentModel
    *           component model
    */
   void addComponentModel(Object componentModel);

   /**
    * Add a key/value pair to this container's Context.
    * 
    * @param key
    *           any unique object valid to the Context's implementation
    * @param value
    *           any object valid to the Context's implementation
    */
   void addContextValue(String key, Object value);

   /**
    * Disposes of this container, which in turn disposes all of it's components. This container should also remove itself from the
    * container hierarchy.
    */
   void dispose();

   /**
    * Returns this container's context. A Context is a simple data store used to hold values which may alter the execution of the
    * Container.
    * 
    * @return this container's context.
    */
   Map<String, Object> getContext();

   /**
    * Returns this container's logger.
    * 
    * @return this container's logger.
    */
   Logger getLogger();

   /**
    * Returns true if this container has a component with the given role/role-hint.
    * 
    * @param type
    *           the non-unique type of the component
    * @return true if this container has a component with the given role/role-hint
    */
   boolean hasComponent(Class<?> type);

   /**
    * Returns true if this container has a component with the given role/role-hint.
    * 
    * @param type
    *           the non-unique type of the component
    * @param roleHint
    *           a hint for the desired component implementation
    * @return true if this container has a component with the given role/role-hint
    */
   boolean hasComponent(Class<?> type, String roleHint);

   /**
    * Looks up and returns a component object with the given unique key or role.
    * 
    * @param type
    *           the unique type of the component within the container
    * @return a Plexus component object
    * @throws ComponentLookupException
    *            when error
    */
   <T> T lookup(Class<T> type) throws ComponentLookupException;

   /**
    * Looks up and returns a component object with the given unique role/role-hint combination.
    * 
    * @param type
    *           the non-unique type of the component
    * @param roleHint
    *           a hint for the desired component implementation
    * @return a Plexus component object
    * @throws ComponentLookupException
    *            when error
    */
   <T> T lookup(Class<T> type, String roleHint) throws ComponentLookupException;

   /**
    * Looks up and returns a List of component objects with the given role.
    * 
    * @param type
    *           the non-unique type of the components
    * @return a List of component objects
    * @throws ComponentLookupException
    *            when error
    */
   <T> List<T> lookupList(Class<T> type) throws ComponentLookupException;

   /**
    * Looks up and returns a List of component objects with the given role.
    * 
    * @param type
    *           the non-unique type of the components
    * @param roleHints
    *           role hints
    * @return a List of component objects
    * @throws ComponentLookupException
    *            when error
    */
   <T> List<T> lookupList(Class<T> type, List<String> roleHints) throws ComponentLookupException;

   /**
    * Looks up and returns a Map of component objects with the given role, keyed by all available role-hints.
    * 
    * @param type
    *           the non-unique type of the components
    * @return a Map of component objects
    * @throws ComponentLookupException
    *            when error
    */
   <T> Map<String, T> lookupMap(Class<T> type) throws ComponentLookupException;

   /**
    * Looks up and returns a Map of component objects with the given role, keyed by all available role-hints.
    * 
    * @param type
    *           the non-unique type of the components
    * @param roleHints
    *           role hints
    * @return a Map of component objects
    * @throws ComponentLookupException
    *            when error
    */
   <T> Map<String, T> lookupMap(Class<T> type, List<String> roleHints) throws ComponentLookupException;

   // ----------------------------------------------------------------------
   // Context
   // ----------------------------------------------------------------------

   /**
    * Releases the component from the container. This is dependent upon how the implementation manages the component, but usually
    * enacts some standard lifecycle shutdown procedure on the component. In every case, the component is no longer accessible from
    * the container (unless another is created).
    * 
    * @param component
    *           the plexus component object to release
    */
   void release(Object component);
}
