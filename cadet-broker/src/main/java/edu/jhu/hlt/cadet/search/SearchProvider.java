/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.search;

import java.util.List;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.search.SearchCapability;
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

    /**
     * Get a list of search type-language pairs
     */
    public List<SearchCapability> getCapabilities() throws ServicesException, TException;

    /**
     * Get a corpus list from the search provider
     */
    public List<String> getCorpora() throws ServicesException, TException;
}
