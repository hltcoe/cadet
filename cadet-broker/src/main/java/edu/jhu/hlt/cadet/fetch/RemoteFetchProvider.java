package edu.jhu.hlt.cadet.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.cadet.pool.ClientPool;
import edu.jhu.hlt.cadet.pool.PoolConfig;
import edu.jhu.hlt.cadet.pool.ServiceConfig;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.access.FetchCommunicationService.Client;
import edu.jhu.hlt.concrete.services.NotImplementedException;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

/**
 * Fetch documents from a remote service that implements the FetchCommunicationService thrift service
 */
public class RemoteFetchProvider implements FetchProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteFetchProvider.class);

    private String host;
    private int port;

    private ClientPool<Client> clientPool;
    private Object clientLock = new Object();

    @Override
    public void init(Config config) {
        host = config.getString(CadetConfig.FETCH_HOST);
        port = config.getInt(CadetConfig.FETCH_PORT);

        logger.info("RemoteFetchProvider HOST: " + host);
        logger.info("RemoteFetchProvider PORT: " + port);

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
    public FetchResult fetch(FetchRequest request) throws ServicesException, TException {
        FetchResult results = null;

        synchronized(clientLock) {
            Client client = getClient();
            try {
                results = client.fetch(request);
                clientPool.returnClient(client);
            } catch (TException ex) {
                logger.warn("client failed for fetch");
                clientPool.invalidateClient(client);
                throw ex;
            }
        }

        return results;
    }

    @Override
    public long getCommunicationCount() throws NotImplementedException, TException {
        long count;
        Client client = getClient();
        try {
            count = client.getCommunicationCount();
            clientPool.returnClient(client);
        } catch (TException ex) {
            logger.warn("client failed for fetch");
            clientPool.invalidateClient(client);
            throw ex;
        }
        return count;
    }

    @Override
    public List<String> getCommunicationIDs(long offset, long count) throws NotImplementedException, TException {
        List<String> ids;
        Client client = getClient();
        try {
            ids = client.getCommunicationIDs(offset, count);
            clientPool.returnClient(client);
        } catch (TException ex) {
            logger.warn("client failed for fetch");
            clientPool.invalidateClient(client);
            throw ex;
        }
        return ids;
    }

    @Override
    public boolean alive() throws TException {
        boolean result;
        Client client = getClient();
        try {
            result = client.alive();
            clientPool.returnClient(client);
        } catch (TException ex) {
            logger.warn("client failed for fetch");
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
            logger.warn("client failed for fetch");
            clientPool.invalidateClient(client);
            throw ex;
        }
        return info;
    }

    private Client getClient() throws ServicesException{
        Client client = clientPool.borrowClient();
        if (client == null) {
            throw new ServicesException("No thrift clients available for fetch");
        }
        return client;
    }

}
