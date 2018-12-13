package org.unidal.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

import org.unidal.mixin.ClassTransformer;

/**
 * This agent class will be loaded in the bootstrap classloader or by SunJdkAttacher.loadAgent().
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
         } catch (IOException e) {
            e.printStackTrace();
            // ignore it
         }
      }

      instrumentation.addTransformer(transformer, true);
      s_instrumentation = instrumentation;
   }
}
