package org.unidal.agent.cat.sample;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unidal.agent.SunJdkAttacher;

public class AllHelloTests extends AbstractHelloTest {
   @Override
   protected void initialize(Set<String> classes) {
      classes.add(getClass().getPackage().getName() + ".hello.HelloAnnotation");
      classes.add(getClass().getPackage().getName() + ".hello.HelloArgumentType");
      classes.add(getClass().getPackage().getName() + ".hello.HelloException");
      classes.add(getClass().getPackage().getName() + ".hello.HelloExpression");
      classes.add(getClass().getPackage().getName() + ".hello.HelloMethod");
      classes.add(getClass().getPackage().getName() + ".hello.HelloOverride");
      classes.add(getClass().getPackage().getName() + ".hello.HelloReturnType");
   }

   @Before
   public void before() throws Exception {
      initialize(s_mixins);

      new SunJdkAttacher().loadAgent(MyAgent.class);
   }

   @Test
   public void test() throws Exception {
      new HelloAnnotationTest().test();
      new HelloArgumentTypeTest().test();
      new HelloExceptionTest().test();
      new HelloExpressionTest().test();
      new HelloMethodTest().test();
      new HelloOverrideTest().test();
      new HelloReturnTypeTest().test();
   }
}
