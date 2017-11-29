/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.feedback.store;

import java.util.HashMap;
import java.util.Map;

import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
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
    public CommunicationFeedback(SearchResult results) throws ConcreteException {
        super(results);
        validate(results);

        data = new HashMap<String, SearchFeedback>();
        for (SearchResultItem result : results.getSearchResultItems()) {
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
    private void validate(SearchResult results) throws ConcreteException {
        for (SearchResultItem result : results.getSearchResultItems()) {
            if (!result.isSetCommunicationId()) {
                throw new ConcreteException("Feedback requires communicationId in each SearchResult");
            }
        }
    }
}
