package org.unidal.initialization;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractModule implements Module {
   private AtomicBoolean m_initialized = new AtomicBoolean();

   protected abstract void execute(ModuleContext ctx) throws Exception;

   @Override
   public void initialize(ModuleContext ctx) throws Exception {
      execute(ctx);
   }

   @Override
   public boolean isInitialized() {
      return m_initialized.get();
   }

   @Override
   public void setInitialized(boolean initialized) {
      m_initialized.set(initialized);
   }

   protected void setup(ModuleContext ctx) throws Exception {
      // no nothing by default
   }
}
