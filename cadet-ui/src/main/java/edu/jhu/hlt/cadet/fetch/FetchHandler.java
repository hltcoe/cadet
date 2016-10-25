package edu.jhu.hlt.cadet.fetch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.access.FetchCommunicationService;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.cadet.fetch.FetchProvider;
import edu.jhu.hlt.concrete.Communication;

public class FetchHandler implements FetchCommunicationService.Iface {
    private static Logger logger = LoggerFactory.getLogger(FetchHandler.class);

    private FetchProvider fetchProvider;

    public FetchHandler() {}

    /**
     * Initialize the handler - must be called before any other methods
     *
     * @param provider
     */
    public void init(FetchProvider provider) {
        fetchProvider = provider;
    }

    @Override
    public FetchResult fetch(FetchRequest request) throws ServicesException, TException {

        logFetchRequest(request);

        FetchResult results = fetchProvider.fetch(request);

        logFetchResult(results);

        return results;
    }

    protected static void logFetchRequest(FetchRequest request) {
        logger.info("Fetch: requesting " + request.getCommunicationIdsSize() + " communications");

        Iterator<String> commIterator = request.getCommunicationIdsIterator();
        while (commIterator.hasNext()) {
            logger.debug("CommId: " + commIterator.next());
        }
    }

    protected static void logFetchResult(FetchResult results) {
        logger.info("Fetch: returning " + results.getCommunicationsSize() + " communications");

        Iterator<Communication> communicationIterator = results.getCommunicationsIterator();
        while (communicationIterator.hasNext()) {
            logger.debug("CommId: " + communicationIterator.next().getId());
        }
    }

    @Override
    public ServiceInfo about() throws TException {
        return fetchProvider.about();
    }

    @Override
    public boolean alive() throws TException {
        return fetchProvider.alive();
    }
}
