package edu.jhu.hlt.concrete.retriever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.access.RetrieveRequest;
import edu.jhu.hlt.concrete.access.RetrieveResults;
import edu.jhu.hlt.concrete.access.Retriever;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

/**
 * Retrieves documents from a remote service that implements the Retrieve thrift service
 */
public class RemoteRetrieverProvider implements RetrieverProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteRetrieverProvider.class);

    private String host;
    private int port;

    private TFramedTransport transport;
    private TCompactProtocol protocol;
    private Retriever.Client client;

    @Override
    public void init(Config config) {
        host = config.getString(CadetConfig.RETRIEVE_HOST);
        port = config.getInt(CadetConfig.RETRIEVE_PORT);

        logger.info("RetrieveHandler HOST: " + host);
        logger.info("RetrieveHandler PORT: " + port);

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new Retriever.Client(protocol);
    }

    @Override
    public RetrieveResults retrieve(RetrieveRequest request) throws ServicesException, TException {
        transport.open();
        RetrieveResults results = client.retrieve(request);
        transport.close();

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
