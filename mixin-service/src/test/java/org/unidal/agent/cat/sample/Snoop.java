package org.unidal.agent.cat.sample;

import java.io.IOException;

import org.unidal.helper.Inets;

public class Snoop {
   public String snoop() throws IOException {
      return "IP " + Inets.IP4.getLocalHostAddress();
   }
}