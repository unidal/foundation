package org.unidal.agent;

import java.io.IOException;
import java.util.jar.JarFile;

public interface ClassWeaver {
   /**
    * A key to identify a class weaver.
    * 
    * @return unique id
    */
   public String getId();

   /**
    * Initialize the weaver, and return a jar file to append to bootstrap class path so that it could be loaded without real class
    * file, or return null.
    * 
    * @return a jar file or null otherwise.
    */
   public JarFile initialize();

   /**
    * Check if a full qualified java <code>className</code> could be woven by the weaver or not.
    * 
    * @param className
    *           a full qualified java class name. i.e. "org.unidal.agent.mixin.sample.greeting.Greeting"
    * @return true if could, false otherwise
    */
   public boolean isEligible(String className);

   /**
    * weave a class <code>className</code> with content <code>classfileBuffer</code>, return null if any error happens.
    * 
    * @param className
    *           class name. i.e. "org.unidal.agent.mixin.sample.greeting.Greeting"
    * @param classfileBuffer
    * @param redefined
    * @return
    * @throws IOException
    */
   public byte[] weave(String className, byte[] classfileBuffer, boolean redefined) throws IOException;

   /**
    * Try to register a <code>className</code>, do nothing if not eligible.
    * 
    * @param className
    *           class to weave. i.e. "org.unidal.agent.mixin.sample.greeting.GreetingMixin"
    */
   public void register(String className);
}
