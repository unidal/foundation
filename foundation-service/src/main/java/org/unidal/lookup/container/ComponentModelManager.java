package org.unidal.lookup.container;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.helper.Files;
import org.unidal.helper.Scanners;
import org.unidal.helper.Scanners.ResourceMatcher;
import org.unidal.lookup.container.model.entity.ComponentModel;
import org.unidal.lookup.container.model.entity.PlexusModel;
import org.unidal.lookup.container.model.transform.DefaultSaxParser;
import org.xml.sax.SAXException;

public class ComponentModelManager {
   private List<PlexusModel> m_models = new ArrayList<PlexusModel>();

   // for test purpose
   private PlexusModel m_model = new PlexusModel();

   private Map<ComponentKey, ComponentModel> m_cache = new HashMap<ComponentKey, ComponentModel>();

   public ComponentModelManager() {
      m_models.add(m_model);
   }

   public void addComponent(ComponentModel component) {
      m_model.addComponent(component);
   }

   public ComponentModel getComponentModel(ComponentKey key) {
      ComponentModel model = m_cache.get(key);

      if (model == null) {
         for (PlexusModel plexus : m_models) {
            for (ComponentModel component : plexus.getComponents()) {
               if (key.matches(component.getRole(), component.getHint())) {
                  model = component;
                  break;
               }
            }

            if (model != null) {
               break;
            }
         }
      }

      if (model != null) {
         m_cache.put(key, model);
      }

      return model;
   }

   public List<String> getRoleHints(String role) {
      List<String> roleHints = new ArrayList<String>();
      Set<String> done = new HashSet<String>();

      for (PlexusModel model : m_models) {
         for (ComponentModel component : model.getComponents()) {
            if (role.equals(component.getRole())) {
               String roleHint = component.getHint();

               if (done.contains(roleHint)) {
                  continue;
               } else {
                  done.add(roleHint);
               }

               roleHints.add(roleHint);
            }
         }
      }

      return roleHints;
   }

   public boolean hasComponentModel(ComponentKey key) {
      return getComponentModel(key) != null;
   }

   private void loadCompoents(URL url) throws IOException, SAXException {
      InputStream in = url.openStream();
      String xml = Files.forIO().readFrom(in, "utf-8");

      if (xml.contains("<plexus>")) {
         try {
            PlexusModel model = DefaultSaxParser.parse(xml);

            m_models.add(model);
         } catch (SAXException e) {
            System.err.println(String.format("Bad plexus resource(%s): %s", url, xml));
            throw new IOException(String.format("Bad plexus resource(%s)! " + e, url), e);
         }
      }
   }

   public void loadComponents(InputStream in) throws Exception {
      if (in != null) {
         try {
            PlexusModel model = DefaultSaxParser.parse(in);

            m_models.add(model);
         } finally {
            in.close();
         }
      }
   }

   public void loadComponentsFromClasspath() throws Exception {
      List<URL> urls = scanComponents();

      for (URL url : urls) {
         loadCompoents(url);
      }
   }

   public void reset() {
      m_model.getComponents().clear();
   }

   List<URL> scanComponents() throws IOException {
      final List<URL> components = new ArrayList<URL>();

      Scanners.forResource().scan("META-INF/plexus", new ResourceMatcher() {
         @Override
         public Direction matches(URL base, String path) {
            if (path.startsWith("components-") && path.endsWith(".xml")) {
               try {
                  components.add(new URL(base + "/" + path));

                  return Direction.MATCHED;
               } catch (Throwable e) {
                  // ignore it
               }
            } else if (path.equals("components.xml")) {
               try {
                  components.add(new URL(base + "/" + path));

                  return Direction.MATCHED;
               } catch (Throwable e) {
                  // ignore it
               }
            }

            return Direction.DOWN;
         }
      });

      return components;
   }

   public void setComponentModel(ComponentKey key, Class<?> clazz) {
      for (PlexusModel model : m_models) {
         ComponentModel component = new ComponentModel();

         component.setRole(key.getRole()).setRoleHint(key.getRoleHint()).setImplementation(clazz.getName());
         model.addComponent(component);
      }
   }
}
