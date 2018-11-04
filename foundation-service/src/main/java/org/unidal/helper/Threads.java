package org.unidal.helper;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;
import org.unidal.lookup.logging.TimedConsoleLoggerManager;

public class Threads {
   private static volatile Manager s_manager = new Manager();

   public static void addListener(ThreadListener listener) {
      s_manager.addListener(listener);
   }

   public static ThreadGroupManager forGroup() {
      return s_manager.getThreadGroupManager("Background");
   }

   public static ThreadGroupManager forGroup(String name) {
      return s_manager.getThreadGroupManager(name);
   }

   public static ThreadPoolManager forPool() {
      return s_manager.getThreadPoolManager();
   }

   public static String getCallerClass() {
      return RunnableThread.m_callerThreadLocal.get();
   }

   public static void removeListener(ThreadListener listener) {
      s_manager.removeListener(listener);
   }

   public static void reset() {
      s_manager.reset();
   }

   /**
    * Sleep for a total <code>timeoutInMillis</code> milli-seconds while <code>when</code> is <code>null</code> or
    * <code>true</code>.
    * 
    * @param when
    *           optional. true to make sleep happen, false to break
    * @param timeoutInMillis
    *           max time to sleep if <code>when</code> condition is null or met
    * @throws InterruptedException
    *            when interrupted
    */
   public static void sleep(AtomicBoolean when, long timeoutInMillis) throws InterruptedException {
      if (when != null && !when.get() || timeoutInMillis <= 0) {
         return;
      }

      long deadline = System.currentTimeMillis() + timeoutInMillis;

      while (when == null || when.get()) {
         TimeUnit.MILLISECONDS.sleep(1);

         if (System.currentTimeMillis() >= deadline) {
            break;
         }
      }
   }

   public static abstract class AbstractThreadListener implements ThreadListener {
      @Override
      public void onThreadGroupCreated(ThreadGroup group, String name) {
         // to be override
      }

      @Override
      public void onThreadPoolCreated(ExecutorService pool, String name) {
         // to be override
      }

      @Override
      public void onThreadStarting(Thread thread, String name) {
         // to be override
      }

      @Override
      public void onThreadStopping(Thread thread, String name) {
         // to be override
      }

      @Override
      public boolean onUncaughtException(Thread thread, Throwable e) {
         // to be override
         return false;
      }
   }

   static class DefaultThreadFactory implements ThreadFactory {
      private ThreadGroup m_threadGroup;

      private String m_name;

      private AtomicInteger m_index = new AtomicInteger();

      private UncaughtExceptionHandler m_handler;

      public DefaultThreadFactory(String name) {
         m_threadGroup = new ThreadGroup(name);
         m_name = name;
      }

      public DefaultThreadFactory(ThreadGroup threadGroup) {
         m_threadGroup = threadGroup;
         m_name = threadGroup.getName();
      }

      public String getName() {
         return m_name;
      }

      @Override
      public Thread newThread(Runnable r) {
         int nextIndex = m_index.getAndIncrement(); // always increase by one
         String threadName;

         if (r instanceof Task) {
            threadName = m_name + "-" + ((Task) r).getName();
         } else {
            threadName = m_name + "-" + nextIndex;
         }

         return new RunnableThread(m_threadGroup, r, threadName, m_handler);
      }

      public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
         m_handler = handler;
      }
   }

   @Named(type = ThreadListener.class, value = "logger")
   public static class LoggerThreadListener implements ThreadListener, LogEnabled {
      private Logger m_logger;

      public LoggerThreadListener() {
      }

      public LoggerThreadListener(Logger logger) {
         m_logger = logger;
      }

      @Override
      public void enableLogging(Logger logger) {
         m_logger = logger;
      }

      @Override
      public void onThreadGroupCreated(ThreadGroup group, String name) {
         m_logger.info(String.format("Thread group(%s) created.", name));
      }

      @Override
      public void onThreadPoolCreated(ExecutorService pool, String name) {
         m_logger.info(String.format("Thread pool(%s) created.", name));
      }

      @Override
      public void onThreadStarting(Thread thread, String name) {
         m_logger.info(String.format("Starting thread(%s) ...", name));
      }

      @Override
      public void onThreadStopping(Thread thread, String name) {
         m_logger.info(String.format("Stopping thread(%s).", name));
      }

      @Override
      public boolean onUncaughtException(Thread thread, Throwable e) {
         m_logger.error(String.format("Uncaught exception thrown out of thread(%s)!", thread.getName()), e);
         return true;
      }
   }

   static class Manager implements UncaughtExceptionHandler {
      private Map<String, ThreadGroupManager> m_groupManagers = new LinkedHashMap<String, Threads.ThreadGroupManager>();

      private List<ThreadListener> m_listeners = new ArrayList<ThreadListener>();

      private ThreadPoolManager m_threadPoolManager;

      public Manager() {
         TimedConsoleLoggerManager.skipClass(getClass());

         Thread shutdownThread = new Thread() {
            @Override
            public void run() {
               shutdownAll();
            }
         };

         m_threadPoolManager = new ThreadPoolManager(this);
         shutdownThread.setDaemon(true);

         try {
            Runtime.getRuntime().addShutdownHook(shutdownThread);
         } catch (Throwable e) {
            // in case of system shutting down, runtime could be null
         }
      }

      public void addListener(ThreadListener listener) {
         m_listeners.add(listener);
      }

      public ThreadGroupManager getThreadGroupManager(String name) {
         ThreadGroupManager groupManager = m_groupManagers.get(name);

         if (groupManager == null) {
            synchronized (this) {
               groupManager = m_groupManagers.get(name);

               if (groupManager != null && !groupManager.isActive()) {
                  m_groupManagers.remove(name);
                  groupManager = null;
               }

               if (groupManager == null) {
                  groupManager = new ThreadGroupManager(this, name);
                  m_groupManagers.put(name, groupManager);

                  onThreadGroupCreated(groupManager.getThreadGroup(), name);
               }
            }
         }

         return groupManager;
      }

      public ThreadPoolManager getThreadPoolManager() {
         return m_threadPoolManager;
      }

      public void onThreadGroupCreated(ThreadGroup group, String name) {
         for (ThreadListener listener : m_listeners) {
            try {
               listener.onThreadGroupCreated(group, name);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      public void onThreadPoolCreated(ExecutorService service, String name) {
         for (ThreadListener listener : m_listeners) {
            try {
               listener.onThreadPoolCreated(service, name);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      public void onThreadStarting(Thread thread, String name) {
         for (ThreadListener listener : m_listeners) {
            try {
               listener.onThreadStarting(thread, name);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      public void onThreadStopped(Thread thread, String name) {
         for (ThreadListener listener : m_listeners) {
            try {
               listener.onThreadStopping(thread, name);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      }

      public void removeListener(ThreadListener listener) {
         m_listeners.remove(listener);
      }

      public void reset() {
         for (ThreadGroupManager groupManager : m_groupManagers.values()) {
            groupManager.shutdown();
         }

         m_groupManagers.clear();
         m_listeners.clear();
      }

      public void shutdownAll() {
         try {
            for (ThreadGroupManager manager : m_groupManagers.values()) {
               manager.shutdown();
            }

            m_threadPoolManager.shutdownAll();
         } catch (Throwable e) {
            // all exceptions during shutdown are ignored
         }
      }

      @Override
      public void uncaughtException(Thread thread, Throwable e) {
         for (ThreadListener listener : m_listeners) {
            boolean handled = listener.onUncaughtException(thread, e);

            if (handled) {
               break;
            }
         }
      }
   }

   static class RunnableThread extends Thread {
      private static ThreadLocal<String> m_callerThreadLocal = new ThreadLocal<String>();

      private Runnable m_target;

      private String m_caller;

      private CountDownLatch m_latch = new CountDownLatch(1);

      public RunnableThread(ThreadGroup threadGroup, Runnable target, String name, UncaughtExceptionHandler handler) {
         super(threadGroup, target, name);

         m_target = target;
         m_caller = getCaller();

         setDaemon(true);
         setUncaughtExceptionHandler(handler);

         if (getPriority() != Thread.NORM_PRIORITY) {
            setPriority(Thread.NORM_PRIORITY);
         }
      }

      public void await() throws InterruptedException {
         m_latch.await();
      }

      private String getCaller() {
         StackTraceElement[] elements = new Exception().getStackTrace();
         String prefix = Threads.class.getName() + "$";

         for (int i = 0; i < elements.length; i++) {
            String className = elements[i].getClassName();

            if (className.startsWith(prefix)) {
               continue;
            }

            int pos = className.lastIndexOf('$');

            if (pos < 0) {
               pos = className.lastIndexOf('.');
            }

            if (pos < 0) {
               return className;
            } else {
               return className.substring(pos + 1);
            }
         }

         return null;
      }

      public Runnable getTarget() {
         return m_target;
      }

      @Override
      public void run() {
         m_callerThreadLocal.set(m_caller);
         s_manager.onThreadStarting(this, getName());
         m_latch.countDown();
         super.run();
         s_manager.onThreadStopped(this, getName());
         m_callerThreadLocal.remove();
      }

      public void shutdown() {
         if (m_target instanceof Task) {
            ((Task) m_target).shutdown();
         } else {
            System.out.println(String.format("Thread(%s) is shutdown! ", getName()));
            interrupt();
         }
      }
   }

   // the sample task is just a code template for copy & paste
   static class SampleTask implements Task {
      private AtomicBoolean m_enabled = new AtomicBoolean(true);

      private CountDownLatch m_latch = new CountDownLatch(1);

      @Override
      public String getName() {
         return getClass().getSimpleName();
      }

      @Override
      public void run() {
         try {
            while (m_enabled.get()) {
               // your job goes here

               TimeUnit.MILLISECONDS.sleep(1000); // sleep 1 second
            }
         } catch (InterruptedException e) {
            // ignore it
         } finally {
            m_latch.countDown();
         }
      }

      @Override
      public void shutdown() {
         m_enabled.set(false);

         try {
            m_latch.await();
         } catch (InterruptedException e) {
            // ignore it
         }
      }
   }

   public static interface Task extends Runnable {
      public String getName();

      public void shutdown();
   }

   public static class ThreadGroupManager {
      private DefaultThreadFactory m_factory;

      private ThreadGroup m_threadGroup;

      private boolean m_active;

      private boolean m_deamon;

      public ThreadGroupManager(UncaughtExceptionHandler handler, String name) {
         m_threadGroup = new ThreadGroup(name);
         m_factory = new DefaultThreadFactory(m_threadGroup);
         m_factory.setUncaughtExceptionHandler(handler);
         m_active = true;
         m_deamon = true;
      }

      public void awaitTermination(long time, TimeUnit unit) {
         long remaining = unit.toNanos(time);

         while (remaining > 0) {
            int len = m_threadGroup.activeCount();
            Thread[] activeThreads = new Thread[len];
            int num = m_threadGroup.enumerate(activeThreads);
            boolean anyAlive = false;

            for (int i = 0; i < num; i++) {
               Thread thread = activeThreads[i];

               if (thread.isAlive()) {
                  anyAlive = true;
                  break;
               }
            }

            if (anyAlive) {
               long slice = 1000 * 1000L;

               // wait for 1 ms
               LockSupport.parkNanos(slice);
               remaining -= slice;
            } else {
               break;
            }
         }
      }

      public ThreadGroup getThreadGroup() {
         return m_threadGroup;
      }

      public boolean isActive() {
         return m_active;
      }

      public ThreadGroupManager nonDaemon() {
         m_deamon = false;
         return this;
      }

      public void shutdown() {
         int len = m_threadGroup.activeCount();
         Thread[] activeThreads = new Thread[len];
         int num = m_threadGroup.enumerate(activeThreads);

         for (int i = 0; i < num; i++) {
            Thread thread = activeThreads[i];

            if (thread instanceof RunnableThread) {
               ((RunnableThread) thread).shutdown();
            } else if (thread.isAlive()) {
               thread.interrupt();
            }
         }

         m_active = false;
      }

      public Thread start(Runnable runnable) {
         return start(runnable, m_deamon);
      }

      public Thread start(Runnable runnable, boolean deamon) {
         Thread thread = m_factory.newThread(runnable);

         thread.setDaemon(deamon);
         thread.start();

         if (thread instanceof RunnableThread) {
            try {
               ((RunnableThread) thread).await();
            } catch (InterruptedException e) {
               // ignore it
            }
         }

         return thread;
      }
   }

   public static interface ThreadListener {
      public void onThreadGroupCreated(ThreadGroup group, String name);

      /**
       * Triggered when a thread pool (ExecutorService) has been created.
       * 
       * @param pool
       *           thread pool
       * @param pattern
       *           thread pool name pattern
       */
      public void onThreadPoolCreated(ExecutorService pool, String pattern);

      /**
       * Triggered when a thread is starting.
       * 
       * @param thread
       *           thread which is starting
       * @param name
       *           thread name
       */
      public void onThreadStarting(Thread thread, String name);

      public void onThreadStopping(Thread thread, String name);

      /**
       * Triggered when an uncaught exception thrown from within a thread.
       * 
       * @param thread
       *           thread which has an uncaught exception thrown
       * @param e
       *           the exception uncaught
       * @return true means the exception is handled, it will be not handled again other listeners, false otherwise.
       */
      public boolean onUncaughtException(Thread thread, Throwable e);
   }

   public static class ThreadPoolManager {
      private UncaughtExceptionHandler m_handler;

      private Map<String, ExecutorService> m_services = new LinkedHashMap<String, ExecutorService>();

      public ThreadPoolManager(UncaughtExceptionHandler handler) {
         m_handler = handler;
      }

      public ExecutorService getCachedThreadPool(String name) {
         ExecutorService service = m_services.get(name);

         if (service != null && service.isShutdown()) {
            m_services.remove(name);
            service = null;
         }

         if (service == null) {
            synchronized (this) {
               service = m_services.get(name);

               if (service == null) {
                  DefaultThreadFactory factory = newThreadFactory(name);
                  service = Executors.newCachedThreadPool(factory);

                  m_services.put(name, service);
                  s_manager.onThreadPoolCreated(service, factory.getName());
               }
            }
         }

         return service;
      }

      public ExecutorService getFixedThreadPool(String name, int nThreads) {
         ExecutorService service = m_services.get(name);

         if (service != null && service.isShutdown()) {
            m_services.remove(name);
            service = null;
         }

         if (service == null) {
            synchronized (this) {
               service = m_services.get(name);

               if (service == null) {
                  DefaultThreadFactory factory = newThreadFactory(name);
                  service = Executors.newFixedThreadPool(nThreads, factory);

                  m_services.put(name, service);
                  s_manager.onThreadPoolCreated(service, factory.getName());
               }
            }
         }

         return service;
      }

      public ScheduledExecutorService getScheduledThreadPool(String name, int nThreads) {
         ExecutorService service = m_services.get(name);

         if (service != null && service.isShutdown()) {
            m_services.remove(name);
            service = null;
         }

         if (service == null) {
            synchronized (this) {
               service = m_services.get(name);

               if (service == null) {
                  DefaultThreadFactory factory = newThreadFactory(name);
                  service = Executors.newScheduledThreadPool(nThreads, factory);

                  m_services.put(name, service);
                  s_manager.onThreadPoolCreated(service, factory.getName());
               }
            }
         }

         return (ScheduledExecutorService) service;
      }

      DefaultThreadFactory newThreadFactory(String name) {
         DefaultThreadFactory factory = new DefaultThreadFactory(name);

         factory.setUncaughtExceptionHandler(m_handler);
         return factory;
      }

      public void shutdownAll() {
         for (ExecutorService service : m_services.values()) {
            service.shutdown();
         }

         boolean allTerminated = false;
         int count = 100;

         while (!allTerminated && count-- > 0) {
            try {
               for (ExecutorService service : m_services.values()) {
                  if (!service.isTerminated()) {
                     service.awaitTermination(10, TimeUnit.MILLISECONDS);
                  }
               }

               allTerminated = true;
            } catch (InterruptedException e) {
               // ignore it
            }
         }

         m_services.clear();
      }
   }
}
