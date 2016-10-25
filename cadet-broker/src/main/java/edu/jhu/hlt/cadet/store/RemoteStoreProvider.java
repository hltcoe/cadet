package edu.jhu.hlt.cadet.store;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.access.StoreCommunicationService;
import edu.jhu.hlt.concrete.services.ServiceInfo;

/**
 * Store documents on a remote service that implements the StoreCommunicationService thrift service
 */
public class RemoteStoreProvider implements StoreProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteStoreProvider.class);

    private String host;
    private int port;

    private TFramedTransport transport;
    private TCompactProtocol protocol;
    private StoreCommunicationService.Client client;

    @Override
    public void init(Config config) {
        host = config.getString(CadetConfig.STORE_HOST);
        port = config.getInt(CadetConfig.STORE_PORT);

        logger.info("RemoteStoreProvider HOST: " + host);
        logger.info("RemoteStoreProvider PORT: " + port);

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new StoreCommunicationService.Client(protocol);
    }

    @Override
    public void close() {
        if (transport.isOpen()) {
            transport.close();
        }
    }

    @Override
    public void store(Communication communication) throws TException {
        logger.info("Storing Comm Id: " + communication.getId());
        transport.open();
        client.store(communication);
        transport.close();
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
