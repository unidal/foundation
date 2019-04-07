package org.unidal.agent.cat.sample;

import java.io.InputStream;
import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.transform.DefaultSaxParser;

public class HelloModelTest extends AbstractHelloTest {
   protected void initialize(Set<String> classNames) throws Exception {
      InputStream in = getClass().getResourceAsStream("hello.xml");
      ClassModel model = DefaultSaxParser.parseEntity(ClassModel.class, in);

      s_classModels.add(model);
   }

   @Test
   public void test() throws Exception {
      Hello hello = new Hello();

      hello.hello("Frankie");
      hello.bye("Frankie");

      expect("<init>, hello, bye");

      Thread.sleep(20);
   }

   @Test
   public void testSnoop() throws Exception {
      new Snoop().snoop();

      expect("<init>, snoop");

      Thread.sleep(20);
   }
}
