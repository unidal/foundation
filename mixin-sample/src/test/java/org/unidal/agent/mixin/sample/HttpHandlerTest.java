package org.unidal.agent.mixin.sample;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unidal.agent.AgentMain;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.mixin.asm.MixinModelAggregator;
import org.unidal.agent.mixin.asm.MixinModelBuilder;
import org.unidal.agent.mixin.model.entity.ClassModel;
import org.unidal.agent.mixin.model.entity.MixinModel;
import org.unidal.agent.mixin.model.transform.DefaultSaxParser;
import org.unidal.agent.mixin.sample.protocol.HttpHandlerMixin;
import org.unidal.asm.util.ASMifier;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.xml.sax.SAXException;

public class HttpHandlerTest {
   @Test
   @Ignore
   public void dump() throws Exception {
      ASMifier.main(new String[] { HttpHandlerMixin.class.getName() });
   }

   @Test
   public void testAggregate() throws SAXException, IOException {
      MixinModel original = DefaultSaxParser.parse(getClass().getResourceAsStream("protocol/plain.xml"));
      MixinModel expected = DefaultSaxParser.parse(getClass().getResourceAsStream("protocol/aggregated.xml"));
      MixinModel actual = new MixinModelAggregator().aggregate(original);

      Assert.assertEquals(expected.toString(), actual.toString());
   }

   @Test
   public void testBuild() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("protocol/plain.xml");
      String xml = Files.forIO().readUtf8String(in);
      MixinModel expected = DefaultSaxParser.parse(xml);
      MixinModel mixin = new MixinModelBuilder().build();
      ClassModel actual = mixin.findClass("sun.net.www.protocol.http.Handler");

      if (actual == null) {
         throw new IllegalStateException("org.unidal.mixin.sample.protocol.HttpHandlerMixin "
               + "is not configured in META-INF/mixin.properties.");
      }

      Assert.assertEquals(expected.toString(), new MixinModel().addClass(actual).toString());
   }

   @Test
   public void testMixin() throws Exception {
      System.setProperty("MIXIN_DEBUG", "false");
      System.setProperty("MIXIN_JARFILE_DEBUG", "false");
      new SunJdkAttacher().loadAgent(AgentMain.class);

      try {
         InputStream in = Urls.forIO().openStream("http://baidu.com/");
         String html = Files.forIO().readUtf8String(in);

         Assert.assertEquals(81, html.length());
      } finally {
         // System.in.read();
      }
   }
}
