package org.unidal.lookup;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.unidal.lookup.container.MyPlexusContainer;

public class ContainerLoader {
   private static AtomicReference<PlexusContainer> s_container = new AtomicReference<PlexusContainer>(null);

   public static void destroy() {
      PlexusContainer container = s_container.get();

      if (container != null) {
         container.dispose();
         s_container.set(null);
      }
   }

   public static PlexusContainer getDefaultContainer() {
      if (s_container.get() == null) {
         getContainer(null);
      }

      return s_container.get();
   }

   public static PlexusContainer getContainer(String configuration) {
      InputStream in = null;

      if (configuration != null) {
         in = ContainerLoader.class.getClassLoader().getResourceAsStream(configuration);
      }

      try {
         MyPlexusContainer container = new MyPlexusContainer(in);

         s_container.set(container);
         return container;
      } catch (Exception e) {
         throw new RuntimeException("Unable to create Plexus container! " + e, e);
      }
   }
}
