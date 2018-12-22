package org.unidal.agent.cat.sample;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.cat.sample.hello.HelloAnnotation;

public class HelloAnnotationTest {
   private static Set<String> s_mixins = new LinkedHashSet<String>();

   private Object[] buildParameters(Method method) {
      Class<?>[] types = method.getParameterTypes();
      Object[] params = new Object[types.length];
      int index = 0;

      for (Class<?> type : types) {
         if (type == Integer.TYPE) {
            params[index] = 0;
         } else if (type == Long.TYPE) {
            params[index] = 0l;
         } else if (type == Float.TYPE) {
            params[index] = 0f;
         } else if (type == Double.TYPE) {
            params[index] = 0d;
         } else if (type == String.class) {
            params[index] = "str" + (index + 1);
         }

         index++;
      }

      return params;
   }

   @Test
   public void test() throws Exception {
      System.setProperty("CAT_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloAnnotation");
      new SunJdkAttacher().loadAgent(MockAgent.class);
      HelloAnnotation type = new HelloAnnotation();
      Method[] methods = HelloAnnotation.class.getMethods();

      for (Method method : methods) {
         if (method.getDeclaringClass() != Object.class) {
            method.invoke(type, buildParameters(method));
         }
      }

      Thread.sleep(1);
   }

   public static class MockAgent {
      public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
         ClassTransformer transformer = new ClassTransformer(inst);

         for (String mixin : s_mixins) {
            transformer.register(mixin);
         }

         inst.addTransformer(transformer, false);
      }
   }
}
