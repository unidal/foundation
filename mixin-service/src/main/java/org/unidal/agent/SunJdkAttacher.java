package org.unidal.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class SunJdkAttacher {
   private static final AtomicBoolean s_initialized = new AtomicBoolean();

   private void addToJar(JarOutputStream out, File baseDir, String path) throws IOException {
      File file = new File(baseDir, path);

      if (file.isDirectory()) {
         String[] children = file.list();

         if (children != null) {
            out.putNextEntry(new ZipEntry(path));

            for (String child : children) {
               addToJar(out, baseDir, path + "/" + child);
            }
         }
      } else if (file.isFile()) {
         JarEntry entry = new JarEntry(path);
         byte[] content = readFile(file);

         out.putNextEntry(entry);
         out.write(content);
         out.closeEntry();
      }
   }

   private Manifest buildManifest(Class<?> agentClass) throws IOException {
      Manifest manifest = new Manifest();
      Attributes attr = manifest.getMainAttributes();

      attr.putValue("Manifest-Version", "1.0");
      attr.putValue("Premain-Class", agentClass.getName());
      attr.putValue("Agent-Class", agentClass.getName());
      attr.putValue("Can-Redefine-Classes", "true");
      attr.putValue("Can-Retransform-Classes", "true");

      return manifest;
   }

   private String getAgentJarPath(Class<?> agentClass) throws IOException, URISyntaxException {
      String path = getAgentPath(agentClass);
      File file = new File(path);

      if (file.isFile()) { // jar file
         AgentMain.debug("Agent jar is %s", file.getCanonicalPath());
         return file.getCanonicalPath();
      } else { // directory
         File tmpJar = File.createTempFile("agent-", ".jar");
         Manifest manifest = buildManifest(agentClass);
         JarOutputStream out = new JarOutputStream(new FileOutputStream(tmpJar), manifest);

         tmpJar.deleteOnExit();

         try {
            addToJar(out, file, "");
         } catch (IOException e) {
            AgentMain.info("[WARN] Get agent jar path failed! %s", e);

            throw e;
         } finally {
            out.close();
         }

         String jarPath = tmpJar.getCanonicalPath();

         AgentMain.debug("Agent jar is %s", jarPath);
         return jarPath;
      }
   }

   private String getAgentPath(Class<?> agentClass) throws URISyntaxException {
      CodeSource codeSource = agentClass.getProtectionDomain().getCodeSource();
      URI uri = codeSource.getLocation().toURI();
      String path = uri.getPath();

      if (path == null || path.length() == 0) {
         String str = uri.toString();
         int pos1 = str.lastIndexOf(':');
         int pos2 = str.indexOf(".jar", pos1 + 1);

         if (pos1 > 0 && pos2 > pos1) {
            path = str.substring(pos1 + 1, pos2 + 4);
         } else {
            AgentMain.info("Bad agent path: %s.", str);
         }
      }

      return path;
   }

   private String getCurrentPid() {
      String jvmName = ManagementFactory.getRuntimeMXBean().getName();
      int index = jvmName.indexOf('@');
      String pid = index < 0 ? null : jvmName.substring(0, index);

      AgentMain.debug("Current PID is %s", pid);
      return pid;
   }

   private URLClassLoader getToolsClassloader() throws IOException {
      String javaHome = System.getProperty("java.home");
      File toolsJarFile = new File(javaHome, "../lib/tools.jar");

      if (!toolsJarFile.isFile()) {
         AgentMain.debug("lib/tools.jar does not found.");
         throw new IllegalStateException("This feature is only available under Sun JDK 1.6 and above!");
      } else {
         AgentMain.debug("Tools jar is %s", toolsJarFile.getCanonicalPath());
      }

      URL url = toolsJarFile.toURI().toURL();
      URLClassLoader classloader = new URLClassLoader(new URL[] { url });

      return classloader;
   }

   public void loadAgent(Class<?> agentClass) throws Exception {
      if (s_initialized.get()) {
         return;
      }

      s_initialized.set(true);

      Object vm = null;
      Class<?> clazz = null;

      try {
         // 1. attach to the JVM with given PID
         String pid = getCurrentPid();
         ClassLoader toolsClassLoader = getToolsClassloader();

         // 2. attach the given agent
         clazz = toolsClassLoader.loadClass("com.sun.tools.attach.VirtualMachine");
         vm = clazz.getMethod("attach", String.class).invoke(null, pid);
         AgentMain.debug("Attached to JVM(%s)", vm.getClass().getName());

         // 3. load agent
         String agentJarPath = getAgentJarPath(agentClass);

         System.setProperty("agent.jar.path", agentJarPath);
         vm.getClass().getMethod("loadAgent", String.class).invoke(vm, agentJarPath);
         AgentMain.debug("Java agent(%s) loaded", agentClass.getName());
      } catch (Exception e) {
         AgentMain.debug(e.getMessage());
         e.printStackTrace();
      } finally {
         if (vm != null) {
            // 3. detach from the VM
            clazz.getMethod("detach").invoke(vm);
            AgentMain.debug("Dettached from JVM(%s)", vm.getClass().getName());
         }
      }
   }

   private byte[] readFile(File file) throws IOException {
      FileInputStream in = new FileInputStream(file);
      byte[] content = new byte[(int) file.length()];
      int total = 0;

      while (true) {
         int size = in.read(content, total, content.length - total);

         if (size <= 0) {
            break;
         }

         total += size;
      }

      in.close();
      return content;
   }
}
