package edu.jhu.hlt.cadet.results;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.cadet.fetch.FetchProvider;
import edu.jhu.hlt.concrete.search.SearchResult;

/**
 * A results plugin provides additional processing or filtering of results sets.
 *
 * Example usages:
 *  * Sending a result set to an alternative annotation UI
 *  * Modifying attributes of the results set before annotation begins
 */
public interface ResultsPlugin extends Provider {

    /**
     * Set the fetch provider in case the plugin needs to request a communication
     *
     * @param provider  Fetch provider
     */
    public void setFetchProvider(FetchProvider provider);

    /**
     * Process the search result
     *
     * @param result  search result object
     * @return whether the search result should be registered with the results store
     */
    public boolean process(SearchResult result);
}
