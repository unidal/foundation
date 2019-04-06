package org.unidal.agent.cat;

import java.io.IOException;
import java.util.jar.JarFile;

import org.unidal.agent.AgentMain;
import org.unidal.agent.ClassWeaver;
import org.unidal.agent.cat.asm.CatClassGenerator;
import org.unidal.agent.cat.asm.CatModelBuilder;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.RootModel;

public class CatClassWeaver implements ClassWeaver {
   public static final String ID = "cat";

   private RootModel m_model = new RootModel();

   private CatModelBuilder m_builder = new CatModelBuilder();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JarFile initialize() {
      m_builder.build(m_model);
      return null;
   }

   @Override
   public boolean isEligible(String className) {
      ClassModel model = m_model.findClass(className);

      return model != null;
   }

   public CatModelBuilder getBuilder() {
      return m_builder;
   }

   @Override
   public byte[] weave(String className, byte[] classfileBuffer, boolean redefined) throws IOException {
      ClassModel model = m_model.findClass(className);
      byte[] result = new CatClassGenerator(model, classfileBuffer).generate(redefined);

      AgentMain.info("[CAT] Class(%s) is transformed.", className);
      return result;
   }
}
