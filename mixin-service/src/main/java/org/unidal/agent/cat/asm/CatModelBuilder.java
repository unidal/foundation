package org.unidal.agent.cat.asm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.unidal.agent.AgentMain;
import org.unidal.agent.cat.CatEnabled;
import org.unidal.agent.cat.CatEvent;
import org.unidal.agent.cat.CatResourceProvider;
import org.unidal.agent.cat.CatTransaction;
import org.unidal.agent.cat.model.entity.ClassModel;
import org.unidal.agent.cat.model.entity.EventModel;
import org.unidal.agent.cat.model.entity.InstrumentModel;
import org.unidal.agent.cat.model.entity.MethodModel;
import org.unidal.agent.cat.model.entity.TransactionModel;
import org.unidal.agent.cat.model.transform.BaseVisitor;

public class CatModelBuilder {
   private static String CONFIGURE_XML = "META-INF/cat.xml";

   private static String CONFIGURE_PROPERTIES = "META-INF/cat.properties";

   private Map<String, Boolean> m_classNames = new LinkedHashMap<String, Boolean>();

   private Map<String, ClassModel> m_classModels = new LinkedHashMap<String, ClassModel>();

   private CatResourceProvider m_provider;

   public CatModelBuilder(CatResourceProvider provider) {
      m_provider = provider;
   }

   private static boolean isAnnotation(String desc, Class<?> clazz) {
      Type type = Type.getType(desc);

      return clazz.getName().equals(type.getClassName());
   }

   public void build(InstrumentModel root) {
      new CatPropertiesLoader().build(root);
      new CatModelLoader().build(root);
   }

   private List<String> split(String str) {
      List<String> list = new ArrayList<String>();
      char delimiter = ',';
      int len = str.length();
      StringBuilder sb = new StringBuilder(len);

      for (int i = 0; i < len + 1; i++) {
         char ch = i == len ? delimiter : str.charAt(i);

         if (ch == delimiter) {
            String item = sb.toString();

            sb.setLength(0);
            item = item.trim();

            if (item.length() == 0) {
               continue;
            }

            list.add(item);
         } else {
            sb.append(ch);
         }
      }

      return list;
   }

   private static class CatEnabledAnnotationVisitor extends AnnotationVisitor {
      private ClassModel m_model;

      public CatEnabledAnnotationVisitor(ClassModel model) {
         super(Opcodes.ASM5);

         m_model = model;
      }

      @Override
      public void visit(String name, Object value) {
         if (name.equals("value") && Boolean.FALSE.equals(value)) {
            m_model.setEnabled(false);
         } else if (name.equals("target")) {
            m_model.setOriginName(m_model.getName());
            m_model.setName((String) value);
         } else {
            m_model.setEnabled(true);
         }
      }
   }

   private static class CatEventAnnotationVisitor extends AnnotationVisitor {
      private EventModel m_event;

      public CatEventAnnotationVisitor(EventModel event) {
         super(Opcodes.ASM5);

         m_event = event;
      }

      @Override
      public void visit(String name, Object value) {
         if ("type".equals(name)) {
            m_event.setType((String) value);
         } else if ("name".equals(name)) {
            m_event.setName((String) value);
         }
      }

      @Override
      public AnnotationVisitor visitArray(String name) {
         if ("keys".equals(name)) {
            return new AnnotationVisitor(Opcodes.ASM5) {
               @Override
               public void visit(String name, Object value) {
                  if (value instanceof String[]) {
                     for (String val : (String[]) value) {
                        m_event.addKey(val);
                     }
                  } else {
                     m_event.addKey((String) value);
                  }
               }
            };
         } else if ("values".equals(name)) {
            return new AnnotationVisitor(Opcodes.ASM5) {
               @Override
               public void visit(String name, Object value) {
                  if (value instanceof String[]) {
                     for (String val : (String[]) value) {
                        m_event.addValue(val);
                     }
                  } else {
                     m_event.addValue((String) value);
                  }
               }
            };
         }

         return null;
      }
   }

   private class CatModelLoader {
      public void build(InstrumentModel root) {
         Collection<ClassModel> models = m_provider.getModels(CONFIGURE_XML);

         for (ClassModel model : models) {
            if (root.findClass(model.getName()) == null) {
               root.addClass(model);
            }
         }

         AgentMain.debug("Instrument model: \r\n%s", root);
      }
   }

   private class CatPropertiesLoader {
      public void build(InstrumentModel root) {
         // step 1: collect cat classes from the class paths
         List<URL> urls = m_provider.getResources(CONFIGURE_PROPERTIES);

         for (URL url : urls) {
            try {
               loadClassNames(url, m_classNames);
            } catch (Throwable t) {
               t.printStackTrace();
            }
         }

         // step 2: build model for cat classes
         for (Map.Entry<String, Boolean> e : m_classNames.entrySet()) {
            if (e.getValue().booleanValue()) { // open
               try {
                  ClassModel model = new ClassModel(e.getKey());

                  new ClassModelBuilder(model).build();
                  root.addClass(model);
               } catch (Throwable t) {
                  t.printStackTrace();
               }
            }
         }

         // step 3: add model from configuration center
         for (ClassModel model : m_classModels.values()) {
            root.addClass(model);
         }

         // step 4: remove unrelated method
         root.accept(new MethodRemovalVisitor());
      }

      private void loadClassNames(URL url, Map<String, Boolean> classes) throws IOException {
         InputStream in = url.openStream();

         try {
            Properties properties = new Properties();

            properties.load(in);

            for (String name : properties.stringPropertyNames()) {
               List<String> items = split(name);

               for (String item : items) {
                  if (item.startsWith("-")) {
                     classes.put(item.substring(1), false);
                  } else {
                     Boolean open = classes.get(item);

                     if (open == null || open.booleanValue()) {
                        classes.put(item, true);
                     }
                  }
               }
            }
         } finally {
            try {
               in.close();
            } catch (IOException e) {
               // ignore it
            }
         }
      }
   }

   private static class CatTransactionAnnotationVisitor extends AnnotationVisitor {
      private TransactionModel m_transaction;

      public CatTransactionAnnotationVisitor(TransactionModel transaction) {
         super(Opcodes.ASM5);

         m_transaction = transaction;
      }

      @Override
      public void visit(String name, Object value) {
         if ("type".equals(name)) {
            m_transaction.setType((String) value);
         } else if ("name".equals(name)) {
            m_transaction.setName((String) value);
         }
      }

      @Override
      public AnnotationVisitor visitArray(String name) {
         if ("keys".equals(name)) {
            return new AnnotationVisitor(Opcodes.ASM5) {
               @Override
               public void visit(String name, Object value) {
                  if (value instanceof String[]) {
                     for (String val : (String[]) value) {
                        m_transaction.addKey(val);
                     }
                  } else {
                     m_transaction.addKey((String) value);
                  }
               }
            };
         } else if ("values".equals(name)) {
            return new AnnotationVisitor(Opcodes.ASM5) {
               @Override
               public void visit(String name, Object value) {
                  if (value instanceof String[]) {
                     for (String val : (String[]) value) {
                        m_transaction.addValue(val);
                     }
                  } else {
                     m_transaction.addValue((String) value);
                  }
               }
            };
         }

         return null;
      }
   }

   public static class ClassModelBuilder extends ClassVisitor {
      private ClassModel m_model;

      public ClassModelBuilder(ClassModel model) {
         super(Opcodes.ASM5);

         m_model = model;
      }

      public void build() throws IOException {
         String binaryClassName = m_model.getName().replace('.', '/');
         int flags = ClassReader.SKIP_FRAMES + ClassReader.SKIP_DEBUG + ClassReader.SKIP_CODE;

         new ClassReader(binaryClassName).accept(this, flags);
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         if (isAnnotation(desc, CatEnabled.class)) {
            return new CatEnabledAnnotationVisitor(m_model);
         }

         return null;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         MethodModel model = m_model.findOrCreateMethod(name, desc);

         return new MethodModelBuilder(model);
      }
   }

   private static class MethodModelBuilder extends MethodVisitor {
      private MethodModel m_method;

      public MethodModelBuilder(MethodModel method) {
         super(Opcodes.ASM5);

         m_method = method;
      }

      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         if (isAnnotation(desc, CatTransaction.class)) {
            TransactionModel transaction = new TransactionModel();

            m_method.setTransaction(transaction);
            return new CatTransactionAnnotationVisitor(transaction);
         } else if (isAnnotation(desc, CatEvent.class)) {
            EventModel event = new EventModel();

            m_method.setEvent(event);
            return new CatEventAnnotationVisitor(event);
         }

         return null;
      }
   }

   public static class MethodRemovalVisitor extends BaseVisitor {
      @Override
      public void visitClass(ClassModel _class) {
         List<MethodModel> methods = _class.getMethods();

         for (int i = methods.size() - 1; i >= 0; i--) {
            MethodModel method = methods.get(i);

            if (method.getTransaction() != null) {
               continue;
            }

            if (method.getEvent() != null) {
               continue;
            }

            methods.remove(i);
         }
      }
   }
}
