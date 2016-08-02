package edu.jhu.hlt.concrete.feedback;

import java.util.HashMap;
import java.util.Map;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class SentenceFeedback extends Feedback {
    private Map<SentenceIdentifier, SearchFeedback> data;

    /**
     * Initialize the Feedback for this search results object
     * 
     * @param results  The SearchResults object for relevance feedback
     * @throws ConcreteException if required data is missing
     */
    public SentenceFeedback(SearchResults results) throws ConcreteException {
        super(results);
        validate(results);

        data = new HashMap<SentenceIdentifier, SearchFeedback>();
        for (SearchResult result : results.getSearchResults()) {
            data.put(new SentenceIdentifier(result.getCommunicationId(), result.getSentenceId()), SearchFeedback.NONE);
        }
    }

    /**
     * Adds feedback for a particular sentence
     * 
     * @param communicationId  the communication to add feedback for
     * @param sentenceId  the sentence to add feedback for
     * @param feedback  the value of the feedback
     * @return was the feedback saved
     */
    public boolean addFeedback(String communicationId, UUID sentenceId, SearchFeedback feedback) {
        SentenceIdentifier id = new SentenceIdentifier(communicationId, sentenceId);
        if (data.containsKey(id)) {
            data.put(id, feedback);
            return true;
        }
        return false;
    }

    /**
     * Get the feedback entries for this search results object
     *
     * @return a map of sentence IDs to feedback values
     */
    public Map<SentenceIdentifier, SearchFeedback> getFeedback() {
        return new HashMap<SentenceIdentifier, SearchFeedback>(data);
    }

    /**
     * Validates that the SearchResults object has the required fields
     */
    private void validate(SearchResults results) throws ConcreteException {
        for (SearchResult result : results.getSearchResults()) {
            if (!result.isSetCommunicationId()) {
                throw new ConcreteException("Feedback requires communicationId in each SearchResult");
            }
            if (!result.isSetSentenceId()) {
                throw new ConcreteException("Feedback requires sentenceId in each SearchResult");
            }
        }
    }
}
