package edu.jhu.hlt.concrete.feedback.store;

import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.util.ConcreteException;

public abstract class Feedback {
    private SearchResults searchResults;

    /**
     * Initialize the Feedback for this search results object
     * 
     * @param results  The SearchResults object for relevance feedback
     * @throws ConcreteException if required data is missing
     */
    public Feedback(SearchResults results) throws ConcreteException {
        validate(results);
        searchResults = results;
    }

    /**
     * Get the search results object
     * 
     * @return reference to search results object
     */
    public SearchResults getSearchResults() {
        return searchResults;
    }

    /**
     * Validates that the SearchResults object has fields required for Feedback
     */
    private void validate(SearchResults results) throws ConcreteException {
        if (!results.isSetSearchQuery()) {
            throw new ConcreteException("Feedback requires searchQuery in SearchResults");
        }
        if (!results.isSetSearchResults()) {
            throw new ConcreteException("Feedback requires searchResults in SearchResults");
        }
    }
}
