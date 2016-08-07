package edu.jhu.hlt.cadet.feedback.store;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class MemoryFeedbackStore implements FeedbackStore {
    private static Logger logger = LoggerFactory.getLogger(MemoryFeedbackStore.class);

    private Map<UUID, CommunicationFeedback> commFeedback;
    private Map<UUID, SentenceFeedback> sentFeedback;

    public MemoryFeedbackStore() {
        commFeedback = Collections.synchronizedMap(new HashMap<UUID, CommunicationFeedback>());
        sentFeedback = Collections.synchronizedMap(new HashMap<UUID, SentenceFeedback>());
    }

    @Override
    public void init(Config config) {}

    @Override
    public void close() {}

    @Override
    public void addSearchResults(SearchResults results) throws ConcreteException {
        validate(results);
        if (isSentenceFeedback(results)) {
            logger.debug("Registering search results for sentence feedback");
            sentFeedback.put(results.getUuid(), new SentenceFeedback(results));
        } else {
            logger.debug("Registering search results for communication feedback");
            commFeedback.put(results.getUuid(), new CommunicationFeedback(results));
        }
    }

    private boolean isSentenceFeedback(SearchResults results) {
        return results.getSearchResults().get(0).getSentenceId() != null;
    }

    private void validate(SearchResults results) throws ConcreteException {
        if (!results.isSetSearchResults() || results.getSearchResultsSize() == 0) {
            throw new ConcreteException("SearchResults must contain search results");
        }
    }

    @Override
    public void addFeedback(UUID uuid, String communicationId, SearchFeedback feedback)  throws FeedbackException {
        if (commFeedback.containsKey(uuid)) {
            if (!commFeedback.get(uuid).addFeedback(communicationId, feedback)) {
                throw new FeedbackException("Cannot find communication to add feedback " + communicationId);
            }
        } else {
            throw new FeedbackException("No search results with uuid " + uuid.getUuidString());
        }
    }

    @Override
    public void addFeedback(UUID uuid, String communicationId, UUID sentenceId,
                    SearchFeedback feedback)  throws FeedbackException {
        if (sentFeedback.containsKey(uuid)) {
            if (!sentFeedback.get(uuid).addFeedback(communicationId, sentenceId, feedback)) {
                throw new FeedbackException("Cannot find sentence to add feedback " + sentenceId.getUuidString());
            }
        } else {
            throw new FeedbackException("No search results with uuid " + uuid.getUuidString());
        }
    }

    @Override
    public Map<String, SearchFeedback> getCommunicationFeedback(UUID uuid) {
        if (commFeedback.containsKey(uuid)) {
            return commFeedback.get(uuid).getFeedback();
        } else {
            logger.info("Could not find communication feedback for uuid " + uuid.getUuidString());
            return null;
        }
    }

    @Override
    public Map<SentenceIdentifier, SearchFeedback> getSentenceFeedback(UUID uuid) {
        if (sentFeedback.containsKey(uuid)) {
            return sentFeedback.get(uuid).getFeedback();
        } else {
            logger.info("Could not find sentence feedback for uuid " + uuid.getUuidString());
            return null;
        }
    }

    @Override
    public Set<CommunicationFeedback> queryCommunicationFeedback(FeedbackQuery query) {
        throw new RuntimeException("Not implemented for memory based store");
    }

    @Override
    public Set<SentenceFeedback> querySentenceFeedback(FeedbackQuery query) {
        throw new RuntimeException("Not implemented for memory based store");
    }

    @Override
    public Set<CommunicationFeedback> getAllCommunicationFeedback() {
        return new HashSet<CommunicationFeedback>(commFeedback.values());
    }

    @Override
    public Set<SentenceFeedback> getAllSentenceFeedback() {
        return new HashSet<SentenceFeedback>(sentFeedback.values());
    }

}
