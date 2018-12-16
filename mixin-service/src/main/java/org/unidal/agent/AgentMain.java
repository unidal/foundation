package org.unidal.agent;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * This agent class will be loaded in the bootstrap class loader or by SunJdkAttacher.loadAgent().
 */
public class AgentMain {
   private static Instrumentation s_instrumentation;

   public static void agentmain(String agentArgs, Instrumentation instrumentation) {
      premain(agentArgs, instrumentation);
   }

   public static Instrumentation getInstrumentation() {
      return s_instrumentation;
   }

   public static void premain(String agentArgs, Instrumentation instrumentation) {
      ClassTransformer transformer = new ClassTransformer(instrumentation);
      String agentJarPath = System.getProperty("agent.jar.path");

      if (agentJarPath != null) {
         try {
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
