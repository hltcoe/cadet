package edu.jhu.hlt.concrete.results;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.search.SearchResults;

/**
 * A results plugin provides additional processing or filtering of results sets.
 *
 * Example usages:
 *  * Sending a results set to an alternative annotation UI
 *  * Modifying attributes of the results set before annotation begins
 */
public interface ResultsPlugin extends Provider {
    /**
     * Process the search result
     *
     * @param results  search results object
     * @return whether the search result should be registered with the results store
     */
    public boolean process(SearchResults results);
}
