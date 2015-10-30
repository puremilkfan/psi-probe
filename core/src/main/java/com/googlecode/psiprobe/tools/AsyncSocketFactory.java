/*
 * Licensed under the GPL License. You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE.
 */

package com.googlecode.psiprobe.tools;

import java.io.IOException;
import java.net.Socket;

/**
 * A factory for creating AsyncSocket objects.
 *
 * @author Vlad Ilyushchenko
 */
public class AsyncSocketFactory {

  /**
   * Creates a new AsyncSocket object.
   *
   * @param server the server
   * @param port the port
   * @param timeout the timeout
   * @return the socket
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static Socket createSocket(String server, int port, long timeout) throws IOException {
    SocketWrapper socketWrapper = new SocketWrapper();
    socketWrapper.server = server;
    socketWrapper.port = port;

    Object sync = new Object();
    Thread socketThread = new Thread(new SocketRunnable(socketWrapper, sync));
    socketThread.setDaemon(true);
    Thread timeoutThread = new Thread(new TimeoutRunnable(sync, timeout * 1000));
    timeoutThread.setDaemon(true);

    timeoutThread.start();
    socketThread.start();

    synchronized (sync) {
      if (socketWrapper.socket == null) {
        try {
          sync.wait(timeout * 1000);
        } catch (InterruptedException e) {
          //
        }
      }
    }

    timeoutThread.interrupt();
    socketThread.interrupt();

    socketWrapper.valid = false;

    if (socketWrapper.getSocket() == null && socketWrapper.exception != null) {
      throw socketWrapper.exception;
    } else if (socketWrapper.getSocket() == null) {
      throw new TimeoutException();
    }

    return socketWrapper.getSocket();
  }

  /**
   * The Class SocketWrapper.
   */
  static class SocketWrapper {

    /** The socket. */
    private Socket socket = null;
    
    /** The server. */
    private String server;
    
    /** The port. */
    private int port;
    
    /** The exception. */
    private IOException exception;
    
    /** The valid. */
    private boolean valid = true;

    /**
     * Gets the socket.
     *
     * @return the socket
     */
    public Socket getSocket() {
      return socket;
    }

    /**
     * Sets the socket.
     *
     * @param socket the new socket
     */
    public void setSocket(Socket socket) {
      this.socket = socket;
    }

    /**
     * Gets the server.
     *
     * @return the server
     */
    public String getServer() {
      return server;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort() {
      return port;
    }

    /**
     * Sets the exception.
     *
     * @param exception the new exception
     */
    public void setException(IOException exception) {
      this.exception = exception;
    }

    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid() {
      return valid;
    }

  }

  /**
   * The Class SocketRunnable.
   */
  static class SocketRunnable implements Runnable {

    /** The socket wrapper. */
    private SocketWrapper socketWrapper;
    
    /** The sync. */
    private final Object sync;

    /**
     * Instantiates a new socket runnable.
     *
     * @param socketWrapper the socket wrapper
     * @param sync the sync
     */
    public SocketRunnable(SocketWrapper socketWrapper, Object sync) {
      this.socketWrapper = socketWrapper;
      this.sync = sync;
    }

    public void run() {
      try {
        socketWrapper.setSocket(new Socket(socketWrapper.getServer(), socketWrapper.getPort()));
        if (!socketWrapper.isValid()) {
          socketWrapper.getSocket().close();
          socketWrapper.setSocket(null);
        }
      } catch (IOException e) {
        socketWrapper.setException(e);
      }
      synchronized (sync) {
        sync.notify();
      }
    }

  }

  /**
   * The Class TimeoutRunnable.
   */
  static class TimeoutRunnable implements Runnable {

    /** The sync. */
    private final Object sync;
    
    /** The timeout. */
    private long timeout;

    /**
     * Instantiates a new timeout runnable.
     *
     * @param sync the sync
     * @param timeout the timeout
     */
    public TimeoutRunnable(Object sync, long timeout) {
      this.sync = sync;
      this.timeout = timeout;
    }

    public void run() {
      try {
        Thread.sleep(timeout);
        synchronized (sync) {
          sync.notify();
        }
      } catch (InterruptedException e) {
        //
      }
    }

  }

}
