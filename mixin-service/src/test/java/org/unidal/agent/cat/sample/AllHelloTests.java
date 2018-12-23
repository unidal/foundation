package org.unidal.agent.cat.sample;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.agent.cat.sample.hello.HelloAnnotation;
import org.unidal.agent.cat.sample.hello.HelloArgumentType;
import org.unidal.agent.cat.sample.hello.HelloException;
import org.unidal.agent.cat.sample.hello.HelloExpression;
import org.unidal.agent.cat.sample.hello.HelloMethod;
import org.unidal.agent.cat.sample.hello.HelloReturnType;

public class AllHelloTests {
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
      System.setProperty("CAT_DEBUG", "false");

      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloAnnotation");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloArgumentType");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloException");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloExpression");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloMethod");
      s_mixins.add(getClass().getPackage().getName() + ".hello.HelloReturnType");

      new SunJdkAttacher().loadAgent(MockAgent.class);

      List<Class<?>> classes = new ArrayList<Class<?>>();

      classes.add(HelloAnnotation.class);
      classes.add(HelloArgumentType.class);
      classes.add(HelloException.class);
      classes.add(HelloExpression.class);
      classes.add(HelloMethod.class);
      classes.add(HelloReturnType.class);

      for (Class<?> clazz : classes) {
         Object instance = clazz.newInstance();
         Method[] methods = instance.getClass().getMethods();

         for (Method method : methods) {
            if (method.getDeclaringClass() != Object.class) {
               try {
                  method.invoke(instance, buildParameters(method));
               } catch (Throwable e) {
                  // ignore it
               }
            }
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
