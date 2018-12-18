package org.unidal.agent.mixin.asm;

import java.io.InputStream;
import java.util.jar.JarFile;

import org.junit.Test;
import org.unidal.agent.mixin.asm.MixinJarFileBuilder;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.transform.DefaultSaxParser;

public class JarFileBuilderTest {
   @Test
   public void test() throws Exception {
      InputStream in = getClass().getResourceAsStream("greeting2.xml");
      MixinModel model = DefaultSaxParser.parse(in);
      MixinJarFileBuilder builder = new MixinJarFileBuilder(model);
      JarFile jarFile = builder.build();

      System.out.println(jarFile.size());
   }
}
