package org.unidal.agent.mixin.asm;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.mixin.asm.MixinModelAggregator;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.transform.DefaultSaxParser;
import org.xml.sax.SAXException;

public class MixinModelAggregatorTest {
   @Test
   public void test() throws SAXException, IOException {
      MixinModel original = DefaultSaxParser.parse(getClass().getResourceAsStream("greeting.xml"));
      MixinModel expected = DefaultSaxParser.parse(getClass().getResourceAsStream("greeting2.xml"));
      MixinModel actual = new MixinModelAggregator().aggregate(original);

      Assert.assertEquals(expected.toString(), actual.toString());
   }
}
