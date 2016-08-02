package edu.jhu.hlt.concrete.send;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.access.Sender;
import edu.jhu.hlt.concrete.services.ServiceInfo;

/**
 * Sends documents to a remote service that implements the Sender thrift service
 */
public class RemoteSenderProvider implements SenderProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteSenderProvider.class);

    private String host;
    private int port;

    private TFramedTransport transport;
    private TCompactProtocol protocol;
    private Sender.Client client;

    public void init(Config config) {
        host = config.getString(CadetConfig.SEND_HOST);
        port = config.getInt(CadetConfig.SEND_PORT);

        logger.info("SendHandler HOST: " + host);
        logger.info("SendHandler PORT: " + port);

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new Sender.Client(protocol);
    }

    @Override
    public void send(Communication communication) throws TException {
        logger.info("Storing Comm Id: " + communication.getId());
        transport.open();
        client.send(communication);
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
