package org.unidal.mixin.asm;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.mixin.model.entity.MixinModel;
import org.unidal.mixin.model.transform.DefaultSaxParser;
import org.unidal.mixin.sample.greeting.GreetingMixin;
import org.unidal.mixin.sample.greeting.GreetingMixin2;
import org.xml.sax.SAXException;

public class MixinModelBuilderTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("greeting.xml");
      MixinModel expected = DefaultSaxParser.parse(in);
      MixinModelBuilder builder = new MixinModelBuilder();

      builder.register(GreetingMixin.class.getName());
      builder.register(GreetingMixin2.class.getName());

      MixinModel actual = builder.build();

      Assert.assertEquals(expected.toString(), actual.toString());
   }
}