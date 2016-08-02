package edu.jhu.hlt.concrete.feedback;

import java.util.HashMap;
import java.util.Map;

import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.util.ConcreteException;

/**
 * Feedback on a list of search results for communications
 *
 * Stores a reference to the original search results object for memory efficiency.
 */
public class CommunicationFeedback extends Feedback {
    private Map<String, SearchFeedback> data;

    /**
     * Initialize the Feedback for this search results object
     * 
     * @param results  The SearchResults object for relevance feedback
     * @throws ConcreteException if required data is missing
     */
    public CommunicationFeedback(SearchResults results) throws ConcreteException {
        super(results);
        validate(results);

        data = new HashMap<String, SearchFeedback>();
        for (SearchResult result : results.getSearchResults()) {
            data.put(result.getCommunicationId(), SearchFeedback.NONE);
        }
    }

    /**
     * Adds feedback for a particular communication
     * 
     * @param communicationId  the communication to add feedback for
     * @param feedback  the value of the feedback
     * @return was the feedback saved
     */
    public boolean addFeedback(String communicationId, SearchFeedback feedback) {
        if (data.containsKey(communicationId)) {
            data.put(communicationId, feedback);
            return true;
        }
        return false;
    }

    /**
     * Get the feedback entries for this search results object
     *
     * @return a map of communication IDs to feedback values
     */
    public Map<String, SearchFeedback> getFeedback() {
        return new HashMap<String, SearchFeedback>(data);
    }

    /**
     * Validates that the SearchResults object has the required fields
     */
    private void validate(SearchResults results) throws ConcreteException {
        for (SearchResult result : results.getSearchResults()) {
            if (!result.isSetCommunicationId()) {
                throw new ConcreteException("Feedback requires communicationId in each SearchResult");
            }
        }
    }
}
