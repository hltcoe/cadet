package edu.jhu.hlt.concrete.retriever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.access.Retriever;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.access.RetrieveResults;
import edu.jhu.hlt.concrete.access.RetrieveRequest;
import edu.jhu.hlt.concrete.Communication;

public class RetrieverHandler implements Retriever.Iface {
    private static Logger logger = LoggerFactory.getLogger(RetrieverHandler.class);

    private RetrieverProvider retrieverProvider;

    public RetrieverHandler() {}

    /**
     * Initialize the handler - must be called before any other methods
     * 
     * @param provider
     */
    public void init(RetrieverProvider provider) {
        retrieverProvider = provider;
    }

    public RetrieveResults retrieve(RetrieveRequest request) throws ServicesException, TException {

        logRetrieveRequest(request);

        RetrieveResults results = retrieverProvider.retrieve(request);

        logRetrieveResults(results);

        return results;
    }

    protected static void logRetrieveRequest(RetrieveRequest request) {
        logger.info("Retrieve: requesting " + request.getCommunicationIdsSize() + " communications");

        Iterator<String> commIterator = request.getCommunicationIdsIterator();
        while (commIterator.hasNext()) {
            logger.debug("CommId: " + commIterator.next());
        }
    }

    protected static void logRetrieveResults(RetrieveResults results) {
        logger.info("Retrieve: returning " + results.getCommunicationsSize() + " communications");
        
        Iterator<Communication> communicationIterator = results.getCommunicationsIterator();
        while (communicationIterator.hasNext()) {
            logger.debug("CommId: " + communicationIterator.next().getId());
        }
    }

    @Override
    public ServiceInfo about() throws TException {
        return retrieverProvider.about();
    }

    @Override
    public boolean alive() throws TException {
        return retrieverProvider.alive();
    }

}
