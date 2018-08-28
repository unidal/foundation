package org.unidal.lookup.container;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentLookupException;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

public class ComponentFactoryTest extends ComponentTestCase {
   @Test
   public void testFactory() throws Exception {
      define(C10.class);
      define(C11.class); // this will be overridden
      define(MockComponentFactory.class);

      I1 c101 = lookup(I1.class);
      I1 c102 = lookup(I1.class);
      I1 c111 = lookup(I1.class, "singleton");
      I1 c112 = lookup(I1.class, "singleton");
      I1 c121 = lookup(I1.class, "per-lookup");
      I1 c122 = lookup(I1.class, "per-lookup");

      Assert.assertSame("singleton should return same instance.", c101, c102);
      Assert.assertSame("singleton should return same instance.", c111, c112);
      Assert.assertNotSame("per-lookup should NOT return same instance.", c121, c122);

      List<I1> list = lookupList(I1.class);
      Map<String, I1> map = lookupMap(I1.class);

      Assert.assertEquals("[C11:false, C12:false, C10:true]", list.toString());
      Assert.assertEquals("{singleton=C11:false, per-lookup=C12:false, default=C10:true}", map.toString());
   }

   @Test
   public void testNormal() throws Exception {
      define(C10.class);
      define(C11.class);
      define(C12.class);

      I1 c101 = lookup(I1.class);
      I1 c102 = lookup(I1.class);
      I1 c111 = lookup(I1.class, "singleton");
      I1 c112 = lookup(I1.class, "singleton");
      I1 c121 = lookup(I1.class, "per-lookup");
      I1 c122 = lookup(I1.class, "per-lookup");

      Assert.assertSame("singleton should return same instance.", c101, c102);
      Assert.assertSame("singleton should return same instance.", c111, c112);
      Assert.assertNotSame("per-lookup should NOT return same instance.", c121, c122);

      List<I1> list = lookupList(I1.class);
      Map<String, I1> map = lookupMap(I1.class);

      Assert.assertEquals("[C10:true, C11:true, C12:true]", list.toString());
      Assert.assertEquals("{default=C10:true, singleton=C11:true, per-lookup=C12:true}", map.toString());
   }

   @Named(type = I1.class)
   public static class C10 implements I1, Initializable {
      private boolean m_flag;

      @Override
      public void initialize() throws InitializationException {
         m_flag = true;
      }

      @Override
      public String toString() {
         return getClass().getSimpleName() + ":" + m_flag;
      }
   }

   @Named(type = I1.class, value = "singleton")
   public static class C11 implements I1, Initializable {
      private boolean m_flag;

      @Override
      public void initialize() throws InitializationException {
         m_flag = true;
      }

      @Override
      public String toString() {
         return getClass().getSimpleName() + ":" + m_flag;
      }
   }

   @Named(type = I1.class, value = "per-lookup", instantiationStrategy = Named.PER_LOOKUP)
   public static class C12 implements I1, Initializable {
      private boolean m_flag;

      @Override
      public void initialize() throws InitializationException {
         m_flag = true;
      }

      @Override
      public String toString() {
         return getClass().getSimpleName() + ":" + m_flag;
      }
   }

   public static interface I1 {
   }

   @Named(type = ComponentFactory.class)
   public static class MockComponentFactory implements ComponentFactory {
      private Map<String, Class<?>> m_map = new HashMap<String, Class<?>>();

      private Map<String, Object> m_cache = new HashMap<String, Object>();

      public MockComponentFactory() {
         m_map.put("singleton", C11.class);
         m_map.put("per-lookup", C12.class);
      }

      @Override
      public List<String> getRoleHints(String role) {
         return Arrays.asList("singleton", "per-lookup");
      }

      @Override
      public boolean hasComponent(String role, String roleHint) {
         return role.equals(I1.class.getName()) && m_map.containsKey(roleHint);
      }

      @Override
      public Object lookup(String role, String roleHint) throws ComponentLookupException {
         if (role.equals(I1.class.getName())) {
            Class<?> clazz = m_map.get(roleHint);

            if (clazz != null) {
               Object instance = m_cache.get(roleHint);

               if (instance == null) {
                  try {
                     instance = clazz.newInstance();
                     m_cache.put(roleHint, instance);
                  } catch (Throwable e) {
                     throw new ComponentLookupException("Error when lookup component due to " + e, role, roleHint);
                  }
               } else if (roleHint.equals("per-lookup")) {
                  try {
                     instance = clazz.newInstance();
                  } catch (Throwable e) {
                     throw new ComponentLookupException("Error when lookup component due to " + e, role, roleHint);
                  }
               }

               return instance;
            }
         }

         throw new ComponentLookupException("Component is not found!", role, roleHint);
      }
   }
}
