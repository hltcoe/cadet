package edu.jhu.hlt.concrete.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TFramedTransport;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.search.Search;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

/**
 * Submits queries against a remote service that implements the Search thrift service
 */
public class RemoteSearchProvider implements SearchProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteSearchProvider.class);

    private String host;
    private int port;

    private TFramedTransport transport;
    private TCompactProtocol protocol;
    private Search.Client client;

    @Override
    public void init(Config config) {
        host = config.getString(CadetConfig.SEARCH_HOST);
        port = config.getInt(CadetConfig.SEARCH_PORT);

        logger.info("SearchHandler HOST: " + host);
        logger.info("SearcheHandler PORT: " + port);

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new Search.Client(protocol);
    }

    @Override
    public SearchResults search(SearchQuery searchQuery) throws ServicesException, TException {
        SearchResults results = null;

        transport.open();
        results = client.search(searchQuery);
        transport.close();

        if (results == null) {
            throw new ServicesException("Invalid results from search provider");
        }

        return results;
    }

    @Override
    public boolean alive() throws TException {
        transport.open();
        boolean result = client.alive();
        transport.close();
        return result;
    }

    @Override
    public ServiceInfo about() throws TException {
        transport.open();
        ServiceInfo info = client.about();
        transport.close();
        return info;
    }

}
