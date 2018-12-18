package org.unidal.agent.cat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.cat.model.entity.RootModel;
import org.unidal.agent.cat.model.transform.DefaultSaxParser;
import org.unidal.agent.cat.sample.hello.HelloService;
import org.xml.sax.SAXException;

public class CatModelBuilderTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("hello.xml");
      RootModel expected = DefaultSaxParser.parse(in);
      CatModelBuilder builder = new CatModelBuilder();

      builder.register(HelloService.class.getName());

      RootModel actual = builder.build();

      Assert.assertEquals(expected.toString(), actual.toString());
   }
}
