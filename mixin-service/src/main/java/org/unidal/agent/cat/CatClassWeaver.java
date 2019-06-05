package org.unidal.agent.cat;

import java.io.IOException;
import java.util.jar.JarFile;

import org.unidal.agent.AgentMain;
import org.unidal.agent.ClassWeaver;
import org.unidal.agent.cat.asm.CatClassGenerator;
import org.unidal.agent.cat.asm.CatModelBuilder;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.InstrumentModel;

public class CatClassWeaver implements ClassWeaver {
   public static final String ID = "cat";

   private InstrumentModel m_model = new InstrumentModel();

   private CatResourceProvider m_provider = new CatResourceProvider();

   @Override
   public String getId() {
      return ID;
   }

   @Override
   public JarFile initialize() {
      CatModelBuilder builder = new CatModelBuilder(m_provider);

      builder.build(m_model);
      return null; // no jar file
   }

   @Override
   public boolean isEligible(String className) {
      ClassModel model = m_model.findClass(className);

      if (model == null) {
         model = m_model.findClass(className + ".class");
      }

      return model != null;
   }

   public void setResourceProvider(CatResourceProvider provider) {
      m_provider = provider;
   }

   @Override
   public byte[] weave(String className, byte[] classfileBuffer, boolean redefined) throws IOException {
      ClassModel model = m_model.findClass(className);
      byte[] result = new CatClassGenerator(model, classfileBuffer).generate(redefined);

      AgentMain.info("[CAT] Class(%s) is transformed.", className);
      return result;
   }
}
