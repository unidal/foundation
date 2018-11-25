package org.unidal.agent;

import java.lang.instrument.Instrumentation;

import org.unidal.mixin.ClassTransformer;

/**
 * This agent class will be loaded in the bootstrap classloader or by SunJdkAttacher.loadAgent().
 */
public class AgentMain {
   private static Instrumentation s_instrumentation;

   public static void agentmain(String agentArgs, Instrumentation instrumentation) {
      ClassTransformer transformer = new ClassTransformer(instrumentation);

      instrumentation.addTransformer(transformer, true);
      s_instrumentation = instrumentation;
   }

   public static Instrumentation getInstrumentation() {
      return s_instrumentation;
   }

   public static void premain(String agentArgs, Instrumentation instrumentation) {
      ClassTransformer transformer = new ClassTransformer(instrumentation);

      instrumentation.addTransformer(transformer, true);
      s_instrumentation = instrumentation;
   }
}
