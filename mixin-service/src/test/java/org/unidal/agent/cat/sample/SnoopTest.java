package org.unidal.agent.cat.sample;

import org.junit.Test;

public class SnoopTest extends AbstractHelloTest {
   @Test
   public void test() throws Exception {
      new Snoop().snoop();

      expect("<init>, snoop");

      Thread.sleep(20);
   }
}
