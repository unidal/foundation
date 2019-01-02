package org.unidal.agent;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * This agent class will be loaded in the bootstrap class loader or by SunJdkAttacher.loadAgent().
 */
public class AgentMain {
   private static volatile Instrumentation s_instrumentation;

   public static void agentmain(String agentArgs, Instrumentation instrumentation) {
      main(agentArgs, instrumentation);
   }

   public static void debug(String pattern, Object... args) {
      if (isDebug()) {
         if (args.length == 0) {
            System.out.println(pattern);
         } else {
            System.out.println(String.format(pattern, args));
         }
      }
   }

   public static Instrumentation getInstrumentation() {
      return s_instrumentation;
   }

   public static void info(String pattern, Object... args) {
      if (args.length == 0) {
         System.out.println(pattern);
      } else {
         System.out.println(String.format(pattern, args));
      }
   }

   public static boolean isDebug() {
      return "true".equals(System.getProperty("AGENT_DEBUG"));
   }

   private static synchronized void main(String agentArgs, Instrumentation instrumentation) {
      if (s_instrumentation != null) {
         ClassTransformer transformer = new ClassTransformer(instrumentation);
         String agentJarPath = System.getProperty("agent.jar.path");

         if (agentJarPath != null) {
            try {
               debug("Added jar(%s) to class path.", agentJarPath);
               instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(agentJarPath));
            } catch (Exception e) {
               // ignore it
               e.printStackTrace();
            }
         }

         instrumentation.addTransformer(transformer, true);
         s_instrumentation = instrumentation;
      }
   }

   public static void premain(String agentArgs, Instrumentation instrumentation) {
      main(agentArgs, instrumentation);
   }
}
