package org.unidal.agent.mixin.asm;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.transform.DefaultSaxParser;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

public class MixinModelTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("../mixin.xml");
      String expected = Files.forIO().readUtf8String(in);
      MixinModel actual = DefaultSaxParser.parse(expected);

      Assert.assertEquals(expected.replace("\r\n", "\n"), actual.toString().replace("\r\n", "\n"));
   }
}
