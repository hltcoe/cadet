package edu.jhu.hlt.concrete.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.feedback.store.FeedbackException;
import edu.jhu.hlt.concrete.feedback.store.FeedbackStore;
import edu.jhu.hlt.concrete.search.Feedback;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class FeedbackHandler implements Feedback.Iface {
    private static Logger logger = LoggerFactory.getLogger(FeedbackHandler.class);

    private FeedbackStore store;

    public FeedbackHandler(FeedbackStore store) {
        this.store = store;
    }

    public FeedbackStore getStore() {
        return store;
    }

    public void setStore(FeedbackStore store) {
        this.store = store;
    }

    @Override
    public void startFeedback(SearchResults results) throws TException {
        logger.info("Enrolling " + results.getUuid().getUuidString() + " into feedback storage");
        try {
            store.addSearchResults(results);
        } catch (ConcreteException ex) {
            throw new TException(ex);
        }
    }

    @Override
    public void addCommunicationFeedback(UUID uuid, String commId, SearchFeedback feedback) throws ServicesException, TException {
        logger.info("Adding feedback for commId " + commId);
        try {
            store.addFeedback(uuid, commId, feedback);
        } catch (FeedbackException ex) {
            throw new TException(ex);
        }
    }

    @Override
    public void addSentenceFeedback(UUID uuid, String commId, UUID sentId, SearchFeedback feedback) throws ServicesException, TException {
        logger.info("Adding feedback for commId " + commId + " and sentenceId " + uuid.getUuidString());
        try {
            store.addFeedback(uuid, commId, sentId, feedback);
        } catch (FeedbackException ex) {
            throw new TException(ex);
        }
    }

    @Override
    public ServiceInfo about() throws TException {
        String desc = "Feedback is stored in " + store.getClass().getSimpleName();
        ServiceInfo info = new ServiceInfo("Feedback Service", "1.0.0");
        info.setDescription(desc);
        return info;
    }

    @Override
    public boolean alive() throws TException {
        return true;
    }

}
