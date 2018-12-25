package org.unidal.agent.cat.sample;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.unidal.agent.ClassTransformer;
import org.unidal.agent.SunJdkAttacher;
import org.unidal.cat.message.Message;
import org.unidal.cat.message.MessageTree;
import org.unidal.cat.message.tree.io.MessageTreePool;
import org.unidal.lookup.ContainerLoader;

public abstract class AbstractHelloTest {
   protected static Set<String> s_mixins = new LinkedHashSet<String>();

   protected static MyMessagePool s_pool = new MyMessagePool();

   @Before
   public void before() throws Exception {
      initialize(s_mixins);

      new SunJdkAttacher().loadAgent(MyAgent.class);
   }

   protected Object[] buildParameters(Method method) {
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

   protected void expect(String expectedames) {
      Assert.assertEquals(expectedames, s_pool.getNames());
   }

   protected void initialize(Set<String> mixins) {
      // override it
   }

   protected void invokeAllMethods(Object instance) throws IllegalAccessException, InvocationTargetException {
      List<Method> methods = Arrays.asList(instance.getClass().getMethods());

      Collections.sort(methods, new Comparator<Method>() {
         @Override
         public int compare(Method o1, Method o2) {
            return o1.toString().compareTo(o2.toString());
         }
      });

      for (Method method : methods) {
         if (method.getDeclaringClass() != Object.class) {
            try {
               method.invoke(instance, buildParameters(method));
            } catch (Exception e) {
               // ignore it
            }
         }
      }
   }

   public static class MyAgent {
      public static void agentmain(String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
         ClassTransformer transformer = new ClassTransformer(inst);

         for (String mixin : s_mixins) {
            transformer.register(mixin);
         }

         inst.addTransformer(transformer, false);
      }
   }

   protected static class MyMessagePool implements MessageTreePool {
      private BlockingQueue<MessageTree> m_queue = new ArrayBlockingQueue<MessageTree>(100);

      private StringBuilder m_sb = new StringBuilder(256);

      public MyMessagePool() {
         // hack MessageTreePool
         ContainerLoader.getDefaultContainer().addComponent(this, MessageTreePool.class, null);
      }

      @Override
      public void feed(MessageTree tree) {
         m_queue.offer(tree);

         onMessage(tree);
      }

      public String getNames() {
         String names = m_sb.toString();

         m_sb.setLength(0);
         return names;
      }

      // override it
      protected void onMessage(MessageTree tree) {
         Message message = tree.getMessage();

         if (!"System".equals(message.getType())) {
            if (m_sb.length() > 0) {
               m_sb.append(", ");
            }

            m_sb.append(message.getName());
         }
      }

      @Override
      public MessageTree poll() throws InterruptedException {
         return m_queue.poll(5, TimeUnit.MILLISECONDS);
      }

      @Override
      public int size() {
         return m_queue.size();
      }
   }
}
