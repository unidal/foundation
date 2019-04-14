package org.unidal.agent.mixin;

import java.io.IOException;
import java.util.jar.JarFile;

import org.unidal.agent.AgentMain;
import org.unidal.agent.ClassWeaver;
import org.unidal.agent.mixin.asm.MixinClassGenerator;
import org.unidal.agent.mixin.asm.MixinJarFileBuilder;
import org.unidal.agent.mixin.asm.MixinModelAggregator;
import org.unidal.agent.mixin.asm.MixinModelBuilder;
import org.unidal.agent.mixin.model.entity.ClassModel;
import org.unidal.agent.mixin.model.entity.MixinModel;

public class MixinClassWeaver implements ClassWeaver {
   public static final String ID = "mixin";

   private MixinModel m_mixin;

   private MixinModelBuilder m_builder = new MixinModelBuilder();

   public MixinModelBuilder getBuilder() {
      return m_builder;
   }

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JarFile initialize() {
      MixinModel source = m_builder.build();

      if (!source.getClasses().isEmpty()) {
         MixinModel aggregated = new MixinModelAggregator().aggregate(source);
         JarFile jarFile = new MixinJarFileBuilder(aggregated).build();

         m_mixin = aggregated;
         return jarFile;
      } else {
         m_mixin = new MixinModel();
         return null;
      }
   }

   @Override
   public boolean isEligible(String className) {
      ClassModel model = m_mixin.findClass(className);

      return model != null;
   }

   @Override
   public byte[] weave(String className, byte[] classfileBuffer, boolean redefined) throws IOException {
      ClassModel model = m_mixin.findClass(className);
      byte[] result = new MixinClassGenerator(model, classfileBuffer).generate(redefined);

      AgentMain.info("[Mixin] Class(%s) is transformed.", className);
      return result;
   }
}
