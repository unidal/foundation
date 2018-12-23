package org.unidal.agent.cat.sample;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.cat.sample.hello.HelloArgumentType;

public class HelloArgumentTypeTest {
   private static Set<String> s_mixins = new LinkedHashSet<String>();

   @Before
   public void before() throws Exception {
      System.setProperty("CAT_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloArgumentType");
      new SunJdkAttacher().loadAgent(MockAgent.class);
   }

   private Object[] buildParameters(Method method) {
      Class<?>[] types = method.getParameterTypes();
      Object[] params = new Object[types.length];
      int index = 0;

      for (Class<?> type : types) {
         if (type == Integer.TYPE) {
            params[index] = 0;
         } else if (type == Long.TYPE) {
            params[index] = 0L;
         } else if (type == Float.TYPE) {
            params[index] = 0F;
         } else if (type == Double.TYPE) {
            params[index] = 0D;
         } else if (type == Character.TYPE) {
            params[index] = (char) 0;
         } else if (type == Byte.TYPE) {
            params[index] = (byte) 0;
         } else if (type == Short.TYPE) {
            params[index] = (short) 0;
         } else if (type == Boolean.TYPE) {
            params[index] = false;
         } else if (type == String.class) {
            params[index] = "str" + index;
         }

         index++;
      }

      return params;
   }

   @Test
   public void test() throws Exception {
      HelloArgumentType instance = new HelloArgumentType();
      Method[] methods = HelloArgumentType.class.getMethods();

      for (Method method : methods) {
         if (method.getDeclaringClass() != Object.class) {
            method.invoke(instance, buildParameters(method));
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
