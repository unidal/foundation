package org.unidal.agent.cat;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.cat.model.entity.InstrumentModel;
import org.unidal.agent.cat.model.transform.DefaultSaxParser;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

public class CatModelTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("cat.xml");
      String expected = Files.forIO().readUtf8String(in);
      InstrumentModel actual = DefaultSaxParser.parse(expected);

      Assert.assertEquals(expected.replace("\r\n", "\n"), actual.toString().replace("\r\n", "\n"));
   }
}
