/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.search;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.cadet.pool.ClientPool;
import edu.jhu.hlt.cadet.pool.PoolConfig;
import edu.jhu.hlt.cadet.pool.ServiceConfig;
import edu.jhu.hlt.concrete.search.SearchCapability;
import edu.jhu.hlt.concrete.search.SearchService;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

/**
 * Submits queries against a remote service that implements the Search thrift service
 */
public class RemoteSearchProvider implements SearchProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteSearchProvider.class);

    private ClientPool<SearchService.Client> clientPool;

    @Override
    public void init(Config config) {
        logger.info("Using custom SearchProvider settings");
        init(config.getString(CadetConfig.SEARCH_HOST_CUSTOM),
             config.getInt(CadetConfig.SEARCH_PORT_CUSTOM));
    }

    public void init(String h, int p) {
        logger.info("SearchHandler HOST: " + h);
        logger.info("SearcheHandler PORT: " + p);

        // default to 10 clients and wait up to 1 second
        PoolConfig pc = new PoolConfig(10, 1000L);
        ServiceConfig sc = new ServiceConfig(h, p);
        clientPool = new ClientPool<SearchService.Client>(pc, sc,
                        transport -> new SearchService.Client(new TCompactProtocol(transport)));
    }

    @Override
    public void close() {
        clientPool.close();
    }

    @Override
    public SearchResult search(SearchQuery searchQuery) throws ServicesException, TException {
        SearchResult results = null;

        SearchService.Client client = getClient();
        try {
            results = client.search(searchQuery);
            clientPool.returnClient(client);
        } catch (TException ex) {
            logger.warn("client failed for search");
            clientPool.invalidateClient(client);
            throw ex;
        }

        if (results == null) {
            throw new ServicesException("Invalid results from search provider");
        }

        return results;
    }

    @Override
    public boolean alive() throws TException {
        boolean result;
        SearchService.Client client = getClient();
        try {
            result = client.alive();
            clientPool.returnClient(client);
        } catch (TException ex) {
            clientPool.invalidateClient(client);
            throw ex;
        }
        return result;
    }

    @Override
    public ServiceInfo about() throws TException {
        ServiceInfo info;
        SearchService.Client client = getClient();
        try {
            info = client.about();
            clientPool.returnClient(client);
        } catch (TException ex) {
            clientPool.invalidateClient(client);
            throw ex;
        }
        return info;
    }

    @Override
    public List<SearchCapability> getCapabilities() throws ServicesException, TException {
        List<SearchCapability> capabilities;
        SearchService.Client client = getClient();
        try {
            capabilities = client.getCapabilities();
            clientPool.returnClient(client);
        } catch (TException ex) {
            clientPool.invalidateClient(client);
            throw ex;
        }
        return capabilities;
    }

    @Override
    public List<String> getCorpora() throws ServicesException, TException {
        List<String> corpora;
        SearchService.Client client = getClient();
        try {
            corpora = client.getCorpora();
            clientPool.returnClient(client);
        } catch (TException ex) {
            clientPool.invalidateClient(client);
            throw ex;
        }
        return corpora;
    }

    private SearchService.Client getClient() throws ServicesException{
        SearchService.Client client = clientPool.borrowClient();
        if (client == null) {
            throw new ServicesException("No thrift clients available for search");
        }
        return client;
    }

}
