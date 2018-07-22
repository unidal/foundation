package org.unidal.lookup.container;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.lookup.container.model.entity.ComponentModel;
import org.unidal.lookup.container.model.entity.PlexusModel;

public class ComponentModelManagerTest {
   private InputStream getPlexusModel(boolean overrideOrigin) {
      PlexusModel plexus = new PlexusModel();
      ComponentModel component = new ComponentModel();

      component.setRole(getClass().getName());
      component.setImplementation(getClass().getName());

      if (overrideOrigin) {
         component.setOverrideOrigin(overrideOrigin);
      }

      plexus.addComponent(component);

      return new ByteArrayInputStream(plexus.toString().getBytes());
   }

   @Test
   public void testOverride() throws Exception {
      ComponentModelManager manager = new ComponentModelManager();

      manager.loadComponents(getPlexusModel(false));
      manager.loadComponents(getPlexusModel(true));

      ComponentModel component = manager.getComponentModel(new ComponentKey(getClass(), null));

      Assert.assertEquals(true, component.getOverrideOrigin());
   }

   @Test
   public void testOverride2() throws Exception {
      ComponentModelManager manager = new ComponentModelManager();

      manager.loadComponents(getPlexusModel(true));
      manager.loadComponents(getPlexusModel(false));

      ComponentModel component = manager.getComponentModel(new ComponentKey(getClass(), null));

      Assert.assertEquals(true, component.getOverrideOrigin());
   }

   @Test
   public void testScan() throws Exception {
      ComponentModelManager manager = new ComponentModelManager();
      List<URL> urls = manager.scanComponents();

      Assert.assertEquals(1, urls.size());
   }
}
