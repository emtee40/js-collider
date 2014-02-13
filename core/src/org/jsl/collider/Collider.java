/*
 * Copyright (C) 2013 Sergey Zubarev, info@js-labs.org
 *
 * This file is a part of JS-Collider framework.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jsl.collider;

import java.io.IOException;

/* Collider public API. Typical usage example:
 * <pre>{@code
 * class ColliderApplication
 * {
 *     public static void main( String [] args )
 *     {
 *         try
 *         {
 *             final Collider collider = Collider.create();
 *             collider.run();
 *         }
 *         catch (IOException ex)
 *         {
 *             ex.printStackTrace();
 *         }
 *     }
 * }</pre>
 */

public abstract class Collider
{
    public static class Config
    {
        public int threadPoolThreads;
        public boolean useDirectBuffers;

        public int socketSendBufSize;
        public int socketRecvBufSize;
        public int forwardReadMaxSize;
        public int inputQueueBlockSize;
        public int inputQueueCacheInitialSize;
        public int inputQueueCacheMaxSize;

        public Config()
        {
            threadPoolThreads = 0; /* by default = number of cores */
            useDirectBuffers  = true;

            socketSendBufSize = 0; /* Use system default settings by default */
            socketRecvBufSize = 0; /* Use system default settings by default */

            forwardReadMaxSize          = (256 * 1024);
            inputQueueBlockSize         = (32 * 1024);
            inputQueueCacheInitialSize  = 64;
            inputQueueCacheMaxSize      = 128;
        }
    }

    private final Config m_config;

    protected Collider( Config config )
    {
        m_config = config;
    }

    public final Config getConfig()
    {
        return m_config;
    }

    /**
     * Starts the collider.
     * Do not return the control back while collider is not stopped.
     */
    public abstract void run();

    /**
     * Stops the running collider.
     * The call is asynchronous, collider can still run some time.
     * All currently registered acceptors and connectors will be removed first.
     * After that all established sessions will be closed
     * (Session.Listener.onConnectionClose() will be called for each).
     * Only after that collider run loop stops and the thread running
     * Collider.run() function gets control back.
     */
    public abstract void stop();

    /**
     * Adds an <tt>Acceptor</tt> to the collider.
     * Additionally to the server socket exceptions
     * throws an IOException in a case if collider already stopped.
     */
    public abstract void addAcceptor( Acceptor acceptor ) throws IOException;

    /**
     * Removes the <tt>Acceptor</tt> from the collider.
     * On return guarantees that no one thread runs
     * <tt>Acceptor.onAcceptorStarted</tt> or <tt>Acceptor.createSessionListener</tt>.
     */
    public abstract void removeAcceptor( Acceptor acceptor ) throws InterruptedException;

    public abstract void addConnector( Connector connector ) throws IOException;
    public abstract void removeConnector( Connector connector ) throws InterruptedException;

    public static Collider create( Config config ) throws IOException
    {
        return new ColliderImpl( config );
    }

    public static Collider create() throws IOException
    {
        return create( new Config() );
    }
}
