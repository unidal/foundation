package org.unidal.agent.mixin.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.xml.SAXClassAdapter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ClassPrinter extends DefaultHandler {
   private int m_depth;

   public static void print(ClassReader reader) {
      reader.accept(new SAXClassAdapter(new ClassPrinter(), false), ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
      m_depth--;
      log("</%s>", localName);
   }

   private void log(String patern, Object... args) {
      StringBuilder sb = new StringBuilder(1204);

      for (int i = 0; i < m_depth; i++) {
         sb.append("   ");
      }

      sb.append(String.format(patern, args));
      System.out.println(sb);
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      StringBuilder sb = new StringBuilder(256);
      int len = attributes.getLength();

      for (int i = 0; i < len; i++) {
         String name = attributes.getLocalName(i);
         String value = attributes.getValue(i);

         sb.append(" ").append(name).append("=\"").append(value).append("\"");
      }

      log("<%s%s>", localName, sb);
      m_depth++;
   }
}
