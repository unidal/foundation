package org.unidal.agent.cat.sample;

import java.io.InputStream;
import java.util.Set;

import org.junit.Test;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.transform.DefaultSaxParser;
import org.unidal.agent.cat.sample.greeting.Greeting;

public class HelloModelTest extends AbstractHelloTest {
   protected void initialize(Set<String> classNames) throws Exception {
      InputStream in = getClass().getResourceAsStream("greeting.xml");
      ClassModel model = DefaultSaxParser.parseEntity(ClassModel.class, in);

      s_classModels.add(model);
   }

   @Test
   public void test() throws Exception {
      Greeting greeting = new Greeting();

      greeting.sayHello("Frankie");
      greeting.sayBye("Frankie");

      expect("<init>, sayHello, sayBye");

      Thread.sleep(20);
   }
}
