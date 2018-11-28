package org.unidal.mixin.sample.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;

import org.unidal.cat.Cat;
import org.unidal.cat.message.Message;
import org.unidal.cat.message.MessageTree;
import org.unidal.cat.message.Transaction;
import org.unidal.cat.message.tree.ForkedTransaction;
import org.unidal.cat.message.tree.MessageTreeHelper;
import org.unidal.cat.message.tree.MyForkedTransaction;
import org.unidal.mixin.MixinMeta;

@MixinMeta(targetClassName = "sun.net.www.protocol.http.Handler")
public class HttpHandlerMixin {
   protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
      Transaction t = Cat.newTransaction("HTTP", url.toExternalForm());
      MessageTree tree = MessageTreeHelper.context().getMessageTree();
      String rootMessageId = tree.getRootMessageId();
      String parentMessageId = tree.getMessageId();
      String messageId = MessageTreeHelper.context().nextMessageId();

      ForkedTransaction forked = new MyForkedTransaction(rootMessageId, parentMessageId);

      forked.setMessageId(messageId);
      t.addChild(forked);
      MessageTreeHelper.context().attach(forked);

      try {
         HttpURLConnection conn = (HttpURLConnection) $_openConnection(url, proxy);

         conn.setRequestProperty("X-CAT-ROOT-ID", rootMessageId != null ? rootMessageId : parentMessageId);
         conn.setRequestProperty("X-CAT-PARENT-ID", parentMessageId);
         conn.setRequestProperty("X-CAT-ID", messageId);

         return new HttpURLConnectionWrapper(conn, t);
      } catch (RuntimeException e) {
         t.setStatus(e);
         t.complete();
         throw e;
      } catch (IOException e) {
         t.setStatus(e);
         t.complete();
         throw e;
      } catch (Error e) {
         t.setStatus(e);
         t.complete();
         throw e;
      } catch (Exception e) {
         t.setStatus(e);
         throw new RuntimeException(e);
      } finally {
         // No forked.close() here, otherwise it becomes Embedded instead of Forked
      }
   }

   protected URLConnection $_openConnection(URL url, Proxy proxy) throws IOException {
      return null;
   }

   private static class HttpURLConnectionWrapper extends HttpURLConnection {
      private HttpURLConnection m_conn;

      private Transaction m_t;

      private InputStream m_in;

      public HttpURLConnectionWrapper(HttpURLConnection conn, Transaction t) {
         super(conn.getURL());

         m_conn = conn;
         m_t = t;
      }

      public void addRequestProperty(String key, String value) {
         m_conn.addRequestProperty(key, value);
      }

      public void connect() throws IOException {
         m_conn.connect();
      }

      @Override
      public void disconnect() {
         m_conn.disconnect();
      }

      public boolean equals(Object obj) {
         return m_conn.equals(obj);
      }

      public boolean getAllowUserInteraction() {
         return m_conn.getAllowUserInteraction();
      }

      public int getConnectTimeout() {
         return m_conn.getConnectTimeout();
      }

      public Object getContent() throws IOException {
         return m_conn.getContent();
      }

      @SuppressWarnings("rawtypes")
      public Object getContent(Class[] classes) throws IOException {
         return m_conn.getContent(classes);
      }

      public String getContentEncoding() {
         return m_conn.getContentEncoding();
      }

      public int getContentLength() {
         return m_conn.getContentLength();
      }

      public long getContentLengthLong() {
         return m_conn.getContentLengthLong();
      }

      public String getContentType() {
         return m_conn.getContentType();
      }

      public long getDate() {
         return m_conn.getDate();
      }

      public boolean getDefaultUseCaches() {
         return m_conn.getDefaultUseCaches();
      }

      public boolean getDoInput() {
         return m_conn.getDoInput();
      }

      public boolean getDoOutput() {
         return m_conn.getDoOutput();
      }

      public long getExpiration() {
         return m_conn.getExpiration();
      }

      public String getHeaderField(int n) {
         return m_conn.getHeaderField(n);
      }

      public String getHeaderField(String name) {
         return m_conn.getHeaderField(name);
      }

      public long getHeaderFieldDate(String name, long Default) {
         return m_conn.getHeaderFieldDate(name, Default);
      }

      public int getHeaderFieldInt(String name, int Default) {
         return m_conn.getHeaderFieldInt(name, Default);
      }

      public String getHeaderFieldKey(int n) {
         return m_conn.getHeaderFieldKey(n);
      }

      public long getHeaderFieldLong(String name, long Default) {
         return m_conn.getHeaderFieldLong(name, Default);
      }

      public Map<String, List<String>> getHeaderFields() {
         return m_conn.getHeaderFields();
      }

      public long getIfModifiedSince() {
         return m_conn.getIfModifiedSince();
      }

      public InputStream getInputStream() throws IOException {
         if (m_in == null) {
            m_in = new InputStreamWrapper(m_conn.getInputStream());
         }

         return m_in;
      }

      public long getLastModified() {
         return m_conn.getLastModified();
      }

      public OutputStream getOutputStream() throws IOException {
         return m_conn.getOutputStream();
      }

      public Permission getPermission() throws IOException {
         return m_conn.getPermission();
      }

      public int getReadTimeout() {
         return m_conn.getReadTimeout();
      }

      public Map<String, List<String>> getRequestProperties() {
         return m_conn.getRequestProperties();
      }

      public String getRequestProperty(String key) {
         return m_conn.getRequestProperty(key);
      }

      public URL getURL() {
         return m_conn.getURL();
      }

      public boolean getUseCaches() {
         return m_conn.getUseCaches();
      }

      public int hashCode() {
         return m_conn.hashCode();
      }

      public void setAllowUserInteraction(boolean allowuserinteraction) {
         m_conn.setAllowUserInteraction(allowuserinteraction);
      }

      public void setConnectTimeout(int timeout) {
         m_conn.setConnectTimeout(timeout);
      }

      public void setDefaultUseCaches(boolean defaultusecaches) {
         m_conn.setDefaultUseCaches(defaultusecaches);
      }

      public void setDoInput(boolean doinput) {
         m_conn.setDoInput(doinput);
      }

      public void setDoOutput(boolean dooutput) {
         m_conn.setDoOutput(dooutput);
      }

      public void setIfModifiedSince(long ifmodifiedsince) {
         m_conn.setIfModifiedSince(ifmodifiedsince);
      }

      public void setReadTimeout(int timeout) {
         m_conn.setReadTimeout(timeout);
      }

      public void setRequestProperty(String key, String value) {
         m_conn.setRequestProperty(key, value);
      }

      public void setUseCaches(boolean usecaches) {
         m_conn.setUseCaches(usecaches);
      }

      public String toString() {
         return m_conn.toString();
      }

      @Override
      public boolean usingProxy() {
         return m_conn.usingProxy();
      }

      private class InputStreamWrapper extends InputStream {
         private InputStream m_in;

         public InputStreamWrapper(InputStream in) {
            m_in = in;
         }

         public int available() throws IOException {
            return m_in.available();
         }

         public void close() throws IOException {
            m_in.close();

            int contentLength = getContentLength();
            String contentEncoding = getContentEncoding();
            String contentType = getContentType();
            int statusCode = getResponseCode();

            if (statusCode > 0) {
               m_t.addData("status", statusCode);
            }

            if (contentType != null) {
               m_t.addData("type", contentType);
            }

            if (contentLength >= 0) {
               m_t.addData("length", contentLength);
            }

            if (contentEncoding != null) {
               m_t.addData("encoding", contentEncoding);
            }

            if (statusCode < 400) {
               m_t.setStatus(Message.SUCCESS);
            } else {
               m_t.setStatus(String.valueOf(statusCode));
            }

            Cat.logEvent("HTTP.Status", String.valueOf(statusCode));
            m_t.complete();
         }

         public boolean equals(Object obj) {
            return m_in.equals(obj);
         }

         public int hashCode() {
            return m_in.hashCode();
         }

         public void mark(int readlimit) {
            m_in.mark(readlimit);
         }

         public boolean markSupported() {
            return m_in.markSupported();
         }

         public int read() throws IOException {
            return m_in.read();
         }

         public int read(byte[] b) throws IOException {
            return m_in.read(b);
         }

         public int read(byte[] b, int off, int len) throws IOException {
            return m_in.read(b, off, len);
         }

         public void reset() throws IOException {
            m_in.reset();
         }

         public long skip(long n) throws IOException {
            return m_in.skip(n);
         }

         public String toString() {
            return m_in.toString();
         }
      }
   }
}
