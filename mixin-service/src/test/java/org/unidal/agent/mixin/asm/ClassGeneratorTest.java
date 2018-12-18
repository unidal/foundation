package org.unidal.agent.mixin.asm;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.unidal.agent.mixin.asm.MixinClassGenerator;
import org.unidal.agent.mixin.asm.ClassXmlPrinter;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.transform.DefaultSaxParser;
import org.unidal.agent.mixin.sample.greeting.Greeting;
import org.unidal.helper.Files;

public class ClassGeneratorTest {
   private static boolean DEBUG = false;

   @Test
   public void test() throws Exception {
      MixinModel model = DefaultSaxParser.parse(getClass().getResourceAsStream("../greeting2.xml"));
      String name = Greeting.class.getName();
      InputStream in = getClass().getResourceAsStream("/" + name.replace('.', '/') + ".class");
      byte[] bytes = Files.forIO().readFrom(in);
      byte[] result = new MixinClassGenerator(model.findClass(name), bytes).generate(false);

      if (DEBUG) {
         ClassXmlPrinter.print(new ClassReader(result));
      }

      Assert.assertEquals(2776, result.length);
   }
}
