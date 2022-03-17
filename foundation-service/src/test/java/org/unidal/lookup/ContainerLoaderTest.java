package org.unidal.lookup;

import org.junit.Assert;
import org.junit.Test;

public class ContainerLoaderTest {
   @Test
   public void test() {
      PlexusContainer c1 = ContainerLoader.getDefaultContainer();
      PlexusContainer c2 = ContainerLoader.getDefaultContainer();
      PlexusContainer c3 = ContainerLoader.getContainer(null);
      PlexusContainer c4 = ContainerLoader.getDefaultContainer();
      PlexusContainer c5 = ContainerLoader.getContainer(null);

      Assert.assertSame(c1, c2);
      Assert.assertNotSame(c2, c3);
      Assert.assertSame(c3, c4);
      Assert.assertNotSame(c4, c5);
   }
}
