package edu.jhu.hlt.cadet.summarization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.services.NotImplementedException;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.summarization.SummarizationCapability;
import edu.jhu.hlt.concrete.summarization.SummarizationService;
import edu.jhu.hlt.concrete.summarization.SummarizationRequest;
import edu.jhu.hlt.concrete.summarization.Summary;
import edu.jhu.hlt.concrete.summarization.SummarySourceType;
import edu.jhu.hlt.concrete.summarization.SummaryConcept;

/**
 * Summarize document(s) from a remote service that implements the SummarizationService thrift service
 */
public class RemoteSummarizationProvider implements SummarizationProvider {
    private static Logger logger = LoggerFactory.getLogger(RemoteSummarizationProvider.class);

    private String host;
    private int port;

    private TFramedTransport transport;
    private TCompactProtocol protocol;
    private SummarizationService.Client client;

    private Object clientLock = new Object();

    @Override
    public void init(Config config) {
        host = config.getString(CadetConfig.SUMMARIZATION_HOST);
        port = config.getInt(CadetConfig.SUMMARIZATION_PORT);

        logger.info("RemoteSummarizationProvider HOST: " + host);
        logger.info("RemoteSummarizationProvider PORT: " + port);

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new SummarizationService.Client(protocol);
    }

    @Override
    public void close() {
        if (transport.isOpen()) {
            transport.close();
        }
    }

    @Override
    public Summary summarize(SummarizationRequest query) throws ServicesException, TException {
        if (!transport.isOpen()) {
            transport.open();
        }

        Summary summary = null;
        synchronized(clientLock) {
            summary = client.summarize(query);
        }

        return summary;
    }

    @Override
    public List<SummarizationCapability> getCapabilities() throws ServicesException, TException {
        if (!transport.isOpen()) {
            transport.open();
        }

        List<SummarizationCapability> capabilities = null;
        synchronized(clientLock) {
            capabilities = client.getCapabilities();
        }

        return capabilities;
    }

    @Override
    public boolean alive() throws TException {
        logger.info("remoteSummarizationProvider.alive()");
        if (!transport.isOpen()) {
            transport.open();
        }
        boolean result = client.alive();
        return result;
    }

    @Override
    public ServiceInfo about() throws TException {
        logger.info("remoteSummarizationProvider.about()");
        if (!transport.isOpen()) {
            transport.open();
        }
        ServiceInfo info = client.about();
        return info;
    }

}
