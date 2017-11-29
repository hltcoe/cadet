/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.store;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.cadet.pool.ClientPool;
import edu.jhu.hlt.cadet.pool.PoolConfig;
import edu.jhu.hlt.cadet.pool.ServiceConfig;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.access.StoreCommunicationService.Client;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

/**
 * Store documents on a remote service that implements the StoreCommunicationService thrift service
 */
public class RemoteStoreProvider implements StoreProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteStoreProvider.class);

    private String host;
    private int port;

    private ClientPool<Client> clientPool;
    private Object clientLock = new Object();

    @Override
    public void init(Config config) {
        host = config.getString(CadetConfig.STORE_HOST);
        port = config.getInt(CadetConfig.STORE_PORT);

        logger.info("RemoteStoreProvider HOST: " + host);
        logger.info("RemoteStoreProvider PORT: " + port);

        // default to 10 clients and wait up to 1 second
        PoolConfig pc = new PoolConfig(10, 1000L);
        ServiceConfig sc = new ServiceConfig(host, port);
        clientPool = new ClientPool<Client>(pc, sc,
                        transport -> new Client(new TCompactProtocol(transport)));
    }

    @Override
    public void close() {
        clientPool.close();
    }

    @Override
    public void store(Communication communication) throws TException {
        logger.info("Storing Comm Id: " + communication.getId());

        synchronized(clientLock) {
            Client client = getClient();
            try {
                client.store(communication);
                clientPool.returnClient(client);
            } catch (TException ex) {
                logger.warn("client failed for store");
                clientPool.invalidateClient(client);
                throw ex;
            }
        }
    }

    @Override
    public boolean alive() throws TException {
        boolean result;
        Client client = getClient();
        try {
            result = client.alive();
            clientPool.returnClient(client);
        } catch (TException ex) {
            logger.warn("client failed for store");
            clientPool.invalidateClient(client);
            throw ex;
        }
        return result;
    }

    @Override
    public ServiceInfo about() throws TException {
        ServiceInfo info;
        Client client = getClient();
        try {
            info = client.about();
            clientPool.returnClient(client);
        } catch (TException ex) {
            logger.warn("client failed for store");
            clientPool.invalidateClient(client);
            throw ex;
        }
        return info;
    }

    private Client getClient() throws ServicesException{
        Client client = clientPool.borrowClient();
        if (client == null) {
            throw new ServicesException("No thrift clients available for store");
        }
        return client;
    }

}
