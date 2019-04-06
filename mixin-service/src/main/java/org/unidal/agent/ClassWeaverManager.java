package org.unidal.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.unidal.agent.cat.CatClassWeaver;
import org.unidal.agent.mixin.MixinClassWeaver;

public class ClassWeaverManager {
   private Map<String, ClassWeaver> m_weavers = new LinkedHashMap<String, ClassWeaver>();

   public ClassWeaverManager() {
      MixinClassWeaver mixin = new MixinClassWeaver();
      CatClassWeaver cat = new CatClassWeaver();

      m_weavers.put(mixin.getId(), mixin);
      m_weavers.put(cat.getId(), cat);
   }

   public ClassWeaver findByClass(String className) {
      for (ClassWeaver weaver : m_weavers.values()) {
         if (weaver.isEligible(className)) {
            return weaver;
         }
      }

      return null;
   }

   public ClassWeaver findById(String id) {
      return m_weavers.get(id);
   }

   public List<JarFile> initialize() {
      List<JarFile> jarFiles = new ArrayList<JarFile>();

      for (ClassWeaver weaver : m_weavers.values()) {
         JarFile jarFile = weaver.initialize();

         if (jarFile != null) {
            jarFiles.add(jarFile);
         }
      }

      return jarFiles;
   }
}
