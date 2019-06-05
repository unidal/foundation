package org.unidal.agent.cat;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.agent.AgentMain;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.InstrumentModel;
import org.unidal.agent.cat.model.transform.DefaultSaxParser;

public class CatResourceProvider {
   public Collection<ClassModel> getModels(String name) {
      Map<String, ClassModel> map = new HashMap<String, ClassModel>();
      List<URL> urls = getResources(name);

      for (URL url : urls) {
         try {
            AgentMain.info("Loading instrument model from %s", url);

            InstrumentModel instrument = DefaultSaxParser.parseEntity(InstrumentModel.class, url.openStream());

            for (Map.Entry<String, ClassModel> e : instrument.getClasses().entrySet()) {
               if (!map.containsKey(e.getKey())) {
                  map.put(e.getKey(), e.getValue());
               }
            }
         } catch (Throwable t) {
            t.printStackTrace();
         }
      }

      return map.values();
   }

   public List<URL> getResources(String name) {
      List<URL> urls = new ArrayList<URL>();

      try {
         ClassLoader loader = AgentMain.class.getClassLoader();

         if (loader != null) {
            List<URL> list = Collections.list(loader.getResources(name));
            int index = 1;

            AgentMain.debug("Agent class loader: " + loader);
            AgentMain.debug("Found %s files of %s in the %s", list.size(), name, loader);

            for (URL url : list) {
               AgentMain.debug("%3s: %s", index++, url);

               if (!urls.contains(url)) {
                  urls.add(url);
               }
            }
         }

         // scan bootstrap and system class loader
         {
            List<URL> list = Collections.list(ClassLoader.getSystemResources(name));
            int index = 1;

            AgentMain.debug("Found %s files of %s in the system class loader", list.size(), name);

            for (URL url : list) {
               AgentMain.debug("%3s: %s", index++, url);

               if (!urls.contains(url)) {
                  urls.add(url);
               }
            }
         }
      } catch (Throwable e) {
         // ignore it
         e.printStackTrace();
      }

      return urls;
   }
}
