package org.unidal.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.unidal.agent.cat.CatClassWeaver;
import org.unidal.agent.mixin.MixinClassWeaver;

public class ClassWeaverManager {
   private List<ClassWeaver> m_weavers = new ArrayList<ClassWeaver>();

   public ClassWeaverManager() {
      m_weavers.add(new MixinClassWeaver());
      m_weavers.add(new CatClassWeaver());
   }

   public ClassWeaver find(String className) {
      for (ClassWeaver weaver : m_weavers) {
         if (weaver.isEligible(className)) {
            return weaver;
         }
      }

      return null;
   }

   public List<JarFile> initialize() {
      List<JarFile> jarFiles = new ArrayList<JarFile>();

      for (ClassWeaver weaver : m_weavers) {
         JarFile jarFile = weaver.initialize();

         if (jarFile != null) {
            jarFiles.add(jarFile);
         }
      }

      return jarFiles;
   }

   public void register(String className) {
      for (ClassWeaver weaver : m_weavers) {
         weaver.register(className);
      }
   }
}
