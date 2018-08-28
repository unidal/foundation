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

   private ComponentFactory m_factory; // external component factory to extend plexus capability

   public ComponentManager(PlexusContainer container, InputStream in) throws Exception {
      m_container = container;
      m_modelManager = new ComponentModelManager();
      m_lifecycle = new ComponentLifecycle(this);

      if (in != null) {
         m_modelManager.loadComponents(in);
      }

      m_modelManager.loadComponentsFromClasspath();

      // keep it at last
      m_loggerManager = lookup(new ComponentKey(LoggerManager.class, null));

      register(new ComponentKey(PlexusContainer.class, null), container);
      register(new ComponentKey(Logger.class, null), m_loggerManager.getLoggerForComponent(""));

      initComponentFactory();
   }

   public void addComponentModel(ComponentModel component) {
      m_modelManager.addComponent(component);

      String role = component.getRole();
      String roleHint = component.getRoleHint();

      if (ComponentFactory.class.getName().equals(role) && (roleHint == null || "default".equals(roleHint))) {
         initComponentFactory();
      }
   }

   public void destroy() {
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

   private void initComponentFactory() {
      // allow external component container(i.e. Spring) to plug in
      ComponentKey key = new ComponentKey(ComponentFactory.class, "default");

      if (hasComponent(key)) {
         try {
            m_factory = lookup(key);
         } catch (ComponentLookupException e) {
            // ignore it
            e.printStackTrace();
         }
      }
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
      if (m_factory != null && m_factory.hasComponent(role, roleHint)) {
         return (T) m_factory.lookup(role, roleHint);
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

      if (m_factory != null) {
         List<String> roleHints = m_factory.getRoleHints(role);

         if (roleHints != null && !roleHints.isEmpty()) {
            for (String roleHint : roleHints) {
               T component = (T) m_factory.lookup(role, roleHint);

               components.add(component);
               done.add(roleHint);
            }
         }
      }

      List<String> roleHints = m_modelManager.getRoleHints(role);

      for (String roleHint : roleHints) {
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

      if (m_factory != null) {
         List<String> roleHints = m_factory.getRoleHints(role);

         if (roleHints != null && !roleHints.isEmpty()) {
            for (String roleHint : roleHints) {
               T component = (T) m_factory.lookup(role, roleHint);

               components.put(roleHint, component);
               done.add(roleHint);
            }
         }
      }

      List<String> roleHints = m_modelManager.getRoleHints(role);

      for (String roleHint : roleHints) {
         if (!done.contains(roleHint)) {
            T component = lookup(new ComponentKey(role, roleHint));

            components.put(roleHint, component);
            done.add(roleHint);
         }
      }

      return components;
   }

   public void register(ComponentKey key, Object component) {
      ComponentBox<Object> box = new ComponentBox<Object>(m_lifecycle).register(key, component);

      m_components.put(key.getRole(), box);
      m_modelManager.setComponentModel(key, component.getClass());
   }

   public void release(Object component) {
      m_lifecycle.stop(component);
   }
}
