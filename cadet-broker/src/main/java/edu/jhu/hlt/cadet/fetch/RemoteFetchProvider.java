package edu.jhu.hlt.cadet.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.access.FetchCommunicationService;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

/**
 * Fetch documents from a remote service that implements the FetchCommunicationService thrift service
 */
public class RemoteFetchProvider implements FetchProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteFetchProvider.class);

    private String host;
    private int port;

    private TFramedTransport transport;
    private TCompactProtocol protocol;
    private FetchCommunicationService.Client client;

    @Override
    public void init(Config config) {
        host = config.getString(CadetConfig.FETCH_HOST);
        port = config.getInt(CadetConfig.FETCH_PORT);

        logger.info("RemoteFetchProvider HOST: " + host);
        logger.info("RemoteFetchProvider PORT: " + port);

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new FetchCommunicationService.Client(protocol);
    }

    @Override
    public void close() {
        if (transport.isOpen()) {
            transport.close();
        }
    }

    @Override
    public FetchResult fetch(FetchRequest request) throws ServicesException, TException {
        transport.open();
        FetchResult results = client.fetch(request);
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
