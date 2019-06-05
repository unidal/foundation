package org.unidal.agent.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.mixin.asm.MixinModelBuilder;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.transform.DefaultSaxParser;
import org.unidal.agent.mixin.sample.greeting.GreetingMixin;
import org.unidal.agent.mixin.sample.greeting.GreetingMixin2;
import org.xml.sax.SAXException;

public class MixinModelBuilderTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("greeting.xml");
      MixinModel expected = DefaultSaxParser.parse(in);
      MixinModelBuilder builder = new MixinModelBuilder(new MixinResourceProvider() {
         @Override
         public Map<String, Boolean> getClasses(String name) {
            Map<String, Boolean> classes = super.getClasses(name);

            classes.put(GreetingMixin.class.getName(), true);
            classes.put(GreetingMixin2.class.getName(), true);
            return classes;
         }
      });

      MixinModel actual = builder.build();

      Assert.assertEquals(expected.toString(), actual.toString());
   }
}
