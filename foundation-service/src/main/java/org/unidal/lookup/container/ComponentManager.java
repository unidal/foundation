package org.unidal.lookup.container;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.ComponentLookupException;
import org.unidal.lookup.PlexusContainer;
import org.unidal.lookup.container.lifecycle.ComponentLifecycle;
import org.unidal.lookup.container.model.entity.ComponentModel;
import org.unidal.lookup.logging.Logger;
import org.unidal.lookup.logging.LoggerManager;

public class ComponentManager {
   // component cache
   // role => map (role hint => component)
   private Map<String, ComponentBox<?>> m_components = new HashMap<String, ComponentBox<?>>();

   private PlexusContainer m_container;

   private ComponentLifecycle m_lifecycle;

   private ComponentModelManager m_modelManager;

   private LoggerManager m_loggerManager;

   private ComponentFactoryManager m_factoryManager;

   public ComponentManager(PlexusContainer container, InputStream in) throws Exception {
      m_container = container;
      m_lifecycle = new ComponentLifecycle(this);
      m_factoryManager = new ComponentFactoryManager();
      m_modelManager = new ComponentModelManager();

      if (in != null) {
         m_modelManager.loadComponents(in);
      }

      m_modelManager.loadComponentsFromClasspath();

      // keep it at last
      m_loggerManager = lookup(new ComponentKey(LoggerManager.class, null));

      register(new ComponentKey(PlexusContainer.class, null), container);
      register(new ComponentKey(Logger.class, null), m_loggerManager.getLoggerForComponent(""));

      m_factoryManager.initialize();
   }

   void addComponentModel(ComponentModel component) {
      m_modelManager.addComponent(component);

      if (ComponentFactory.class.getName().equals(component.getRole())) {
         m_factoryManager.initialize();
      }
   }

   void destroy() {
      for (ComponentBox<?> box : m_components.values()) {
         box.destroy();
      }

      m_components.clear();
      m_modelManager.reset();
   }

   public PlexusContainer getContainer() {
      return m_container;
   }

   public LoggerManager getLoggerManager() {
      return m_loggerManager;
   }

   public boolean hasComponent(ComponentKey key) {
      return m_modelManager.hasComponentModel(key);
   }

   public void log(String pattern, Object... args) {
      if ("true".equals(m_container.getContext().get("verbose"))) {
         Logger logger = m_loggerManager.getLoggerForComponent(null);

         logger.info(String.format(pattern, args));
      }
   }

   @SuppressWarnings("unchecked")
   public <T> T lookup(ComponentKey key) throws ComponentLookupException {
      String role = key.getRole();
      String roleHint = key.getRoleHint();
      ComponentBox<?> box = m_components.get(role);

      if (box == null) {
         box = new ComponentBox<T>(m_lifecycle);
         m_components.put(role, box);
      }

      // external factory takes priority
      if (m_factoryManager.hasComponent(role, roleHint)) {
         return (T) m_factoryManager.lookup(role, roleHint);
      } else {
         ComponentModel model = m_modelManager.getComponentModel(key);

         if (model != null) {
            return (T) box.lookup(model);
         } else {
            throw new ComponentLookupException("No component defined!", role, roleHint);
         }
      }
   }

   @SuppressWarnings("unchecked")
   public <T> List<T> lookupList(String role) throws ComponentLookupException {
      List<T> components = new ArrayList<T>();
      Set<String> done = new HashSet<String>();
      List<String> roleHints1 = m_factoryManager.getRoleHints(role);

      if (roleHints1 != null) {
         for (String roleHint : roleHints1) {
            T component = (T) m_factoryManager.lookup(role, roleHint);

            components.add(component);
            done.add(roleHint);
         }
      }

      List<String> roleHints2 = m_modelManager.getRoleHints(role);

      for (String roleHint : roleHints2) {
         if (!done.contains(roleHint)) {
            T component = lookup(new ComponentKey(role, roleHint));

            components.add(component);
            done.add(roleHint);
         }
      }

      return components;
   }

   @SuppressWarnings("unchecked")
   public <T> Map<String, T> lookupMap(String role) throws ComponentLookupException {
      Map<String, T> components = new LinkedHashMap<String, T>();
      Set<String> done = new HashSet<String>();
      List<String> roleHints1 = m_factoryManager.getRoleHints(role);

      if (roleHints1 != null && !roleHints1.isEmpty()) {
         for (String roleHint : roleHints1) {
            T component = (T) m_factoryManager.lookup(role, roleHint);

            components.put(roleHint, component);
            done.add(roleHint);
         }
      }

      List<String> roleHints2 = m_modelManager.getRoleHints(role);

      for (String roleHint : roleHints2) {
         if (!done.contains(roleHint)) {
            T component = lookup(new ComponentKey(role, roleHint));

            components.put(roleHint, component);
            done.add(roleHint);
         }
      }

      return components;
   }

   void register(ComponentKey key, Object component) {
      ComponentBox<Object> box = new ComponentBox<Object>(m_lifecycle).register(key, component);

      m_components.put(key.getRole(), box);
      m_modelManager.setComponentModel(key, component.getClass());
   }

   void release(Object component) {
      m_lifecycle.stop(component);
   }

   private class ComponentFactoryManager {
      // in order
      private List<ComponentFactory> m_factories = new ArrayList<ComponentFactory>();

      public List<String> getRoleHints(String role) {
         List<String> roleHints = new ArrayList<String>();

         for (ComponentFactory factory : m_factories) {
            try {
               List<String> hints = factory.getRoleHints(role);

               if (hints != null) {
                  for (String hint : hints) {
                     if (!roleHints.contains(hint)) {
                        roleHints.add(hint);
                     }
                  }
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         return roleHints;
      }

      public boolean hasComponent(String role, String roleHint) {
         for (ComponentFactory factory : m_factories) {
            try {
               if (factory.hasComponent(role, roleHint)) {
                  return true;
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         return false;
      }

      public void initialize() {
         String role = ComponentFactory.class.getName();
         List<String> roleHints = m_modelManager.getRoleHints(role);
         List<ComponentFactory> factories = new ArrayList<ComponentFactory>();

         for (String roleHint : roleHints) {
            try {
               ComponentFactory component = ComponentManager.this.lookup(new ComponentKey(role, roleHint));

               factories.add(component);
            } catch (ComponentLookupException e) {
               e.printStackTrace();
            }
         }

         m_factories = factories;
      }

      public Object lookup(String role, String roleHint) throws ComponentLookupException {
         for (ComponentFactory factory : m_factories) {
            try {
               if (factory.hasComponent(role, roleHint)) {
                  return factory.lookup(role, roleHint);
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         throw new ComponentLookupException("No component defined!", role, roleHint);
      }
   }
}
