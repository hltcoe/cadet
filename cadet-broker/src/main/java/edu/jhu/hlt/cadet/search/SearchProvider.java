package edu.jhu.hlt.cadet.search;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public interface SearchProvider extends Provider {
    /**
     * Search over communications
     *
     * @param searchQuery  a query object with search parameters
     * @return search results
     */
    public SearchResult search(SearchQuery searchQuery) throws ServicesException, TException;

    /**
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get information about the search provider
     */
    public ServiceInfo about() throws TException;
}
