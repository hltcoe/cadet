/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.feedback.store;

import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.util.ConcreteException;

public abstract class Feedback {
    private SearchResult searchResults;

    /**
     * Initialize the Feedback for this search results object
     *
     * @param results  The SearchResults object for relevance feedback
     * @throws ConcreteException if required data is missing
     */
    public Feedback(SearchResult results) throws ConcreteException {
        validate(results);
        searchResults = results;
    }

    /**
     * Get the search results object
     *
     * @return reference to search results object
     */
    public SearchResult getSearchResults() {
        return searchResults;
    }

    /**
     * Validates that the SearchResults object has fields required for Feedback
     */
    private void validate(SearchResult results) throws ConcreteException {
        if (!results.isSetSearchQuery()) {
            throw new ConcreteException("Feedback requires searchQuery in SearchResults");
        }
        if (!results.isSetSearchResultItems()) {
            throw new ConcreteException("Feedback requires searchResults in SearchResults");
        }
    }
}
