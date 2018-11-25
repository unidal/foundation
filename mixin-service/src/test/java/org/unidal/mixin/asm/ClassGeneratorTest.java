package org.unidal.mixin.asm;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.unidal.helper.Files;
import org.unidal.mixin.model.entity.MixinModel;
import org.unidal.mixin.model.transform.DefaultSaxParser;
import org.unidal.mixin.sample.greeting.Greeting;

public class ClassGeneratorTest {
   private static boolean DEBUG = true;

   @Test
   public void test() throws Exception {
      MixinModel model = DefaultSaxParser.parse(getClass().getResourceAsStream("greeting2.xml"));
      String name = Greeting.class.getName();
      InputStream in = getClass().getResourceAsStream("/" + name.replace('.', '/') + ".class");
      byte[] bytes = Files.forIO().readFrom(in);
      byte[] result = new ClassGenerator(model.findClass(name), bytes).generate(false);

      if (DEBUG) {
         ClassPrinter.print(new ClassReader(result));
      }

      Assert.assertEquals(2916, result.length);
   }
}
