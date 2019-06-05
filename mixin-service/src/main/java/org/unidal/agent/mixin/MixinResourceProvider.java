package org.unidal.agent.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.unidal.agent.AgentMain;

public class MixinResourceProvider {
   public Map<String, Boolean> getClasses(String name) {
      Map<String, Boolean> classes = new LinkedHashMap<String, Boolean>();
      List<URL> urls = getResources(name);

      for (URL url : urls) {
         try {
            loadMixinClasses(url, classes);
         } catch (Throwable t) {
            t.printStackTrace();
         }
      }

      return classes;
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

   private void loadMixinClasses(URL url, Map<String, Boolean> classes) throws IOException {
      InputStream in = url.openStream();

      try {
         Properties properties = new Properties();

         properties.load(in);

         for (String name : properties.stringPropertyNames()) {
            List<String> items = split(name);

            for (String item : items) {
               if (item.startsWith("-")) {
                  classes.put(item.substring(1), false);
               } else {
                  Boolean open = classes.get(item);

                  if (open == null || open.booleanValue()) {
                     classes.put(item, true);
                  }
               }
            }
         }
      } finally {
         try {
            in.close();
         } catch (IOException e) {
            // ignore it
         }
      }
   }

   private List<String> split(String str) {
      List<String> list = new ArrayList<String>();
      char delimiter = ',';
      int len = str.length();
      StringBuilder sb = new StringBuilder(len);

      for (int i = 0; i < len + 1; i++) {
         char ch = i == len ? delimiter : str.charAt(i);

         if (ch == delimiter) {
            String item = sb.toString();

            sb.setLength(0);
            item = item.trim();

            if (item.length() == 0) {
               continue;
            }

            list.add(item);
         } else {
            sb.append(ch);
         }
      }

      return list;
   }
}
