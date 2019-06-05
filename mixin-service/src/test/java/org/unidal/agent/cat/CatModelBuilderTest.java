package org.unidal.agent.cat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.agent.cat.asm.CatModelBuilder;
import org.unidal.agent.cat.asm.CatModelBuilder.ClassModelBuilder;
import org.unidal.agent.cat.asm.CatModelBuilder.MethodRemovalVisitor;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.InstrumentModel;
import org.unidal.agent.cat.model.transform.DefaultSaxParser;
import org.xml.sax.SAXException;

public class CatModelBuilderTest {
   @Test
   public void test() throws SAXException, IOException {
      InputStream in = getClass().getResourceAsStream("hello.xml");
      InstrumentModel expected = DefaultSaxParser.parse(in);
      CatModelBuilder builder = new CatModelBuilder(new CatResourceProvider() {
         @Override
         public Collection<ClassModel> getModels(String name) {
            List<ClassModel> models = new ArrayList<ClassModel>();

            try {
               ClassModel model = new ClassModel(HelloService.class.getName());

               new ClassModelBuilder(model).build();
               model.accept(new MethodRemovalVisitor());
               models.add(model);
            } catch (Throwable t) {
               t.printStackTrace();
            }

            return models;
         }
      });

      InstrumentModel actual = new InstrumentModel();

      builder.build(actual);

      Assert.assertEquals(expected.toString(), actual.toString());
   }

   @CatEnabled(target = "target.full.class.name")
   public static class HelloService {
      @CatTransaction(type = "Service", name = "hello", //
            keys = { "value", "return" }, values = { "${arg0}", "${return}" })
      @CatEvent(type = "Hello", name = "${arg0}")
      public String hello(String value) {
         return value;
      }
   }
}
