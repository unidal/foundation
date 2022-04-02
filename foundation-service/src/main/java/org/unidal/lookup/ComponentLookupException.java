package org.unidal.lookup;

public class ComponentLookupException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   private static String LS = System.getProperty("line.separator");

   private String m_role;

   private String m_roleHint;

   public ComponentLookupException(String message, String role, String roleHint) {
      super(message);

      m_role = role;
      m_roleHint = roleHint;
   }

   public ComponentLookupException(String message, String role, String roleHint, Throwable cause) {
      super(message, cause);

      m_role = role;
      m_roleHint = roleHint;
   }

   @Override
   public String getMessage() {
      StringBuilder sb = new StringBuilder(1024);

      sb.append(super.getMessage()).append(LS); //
      sb.append("      role: ").append(m_role).append(LS); //
      sb.append("  roleHint: ").append(m_roleHint);

      return sb.toString();
   }
}
