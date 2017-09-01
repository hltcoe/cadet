package edu.jhu.hlt.cadet.pool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.hlt.concrete.services.ServicesException;

public class ClientPool<T extends TServiceClient> {
    private static Logger logger = LoggerFactory.getLogger(ClientPool.class);

    private final LinkedBlockingDeque<T> pool;
    private int numCreatedClients = 0;
    private final PoolConfig config;
    private final ServiceConfig service;
    private final Function<TTransport, T> factory;

    public ClientPool(PoolConfig config, ServiceConfig service, Function<TTransport, T> factory) {
        this.config = config;
        this.service = service;
        this.factory = factory;
        pool = new LinkedBlockingDeque<T>();
    }

    /**
     * Borrow a thrift client from the pool
     *
     * @return thrift client or null if none are available within the specified timeout
     * @throws ServicesException if the service is unavailable
     */
    public T borrowClient() throws ServicesException {
        T client = pool.pollFirst();
        if (client == null) {
            if (numCreatedClients < config.getSize()) {
                // create a client
                client = createClient();
                numCreatedClients++;
            } else {
                // wait on client to be returned
                try {
                    client = pool.pollFirst(config.getTimeout(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // swallow exception
                    logger.info("Ran out of clients for " + this.getClass().getCanonicalName());
                }
            }
        }

        return client;
    }

    public void returnClient(T client) {
        pool.addFirst(client);
    }

    public void invalidateClient(T client) {
        closeTransport(client);
        numCreatedClients--;
    }

    public void close() {
        T client = null;
        while ((client = pool.poll()) != null) {
            closeTransport(client);
        }
    }

    private T createClient() throws ServicesException {
        return factory.apply(getTransport(service));
    }

    private TTransport getTransport(ServiceConfig service) throws ServicesException {
        TSocket socket = new TSocket(service.getHost(), service.getPort());
        TTransport transport = new TFramedTransport(socket, Integer.MAX_VALUE);
        try {
            transport.open();
            return transport;
        } catch (TTransportException e) {
            throw new ServicesException("Unable to contact service at " + service.getHost()
                + ":" + service.getPort());
        }
    }

    private void closeTransport(T client) {
        TProtocol protocol = client.getInputProtocol();
        if (protocol != null) {
            TTransport transport = protocol.getTransport();
            if (transport != null) {
                transport.close();
            }
        }
    }
}
