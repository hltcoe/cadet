package edu.jhu.hlt.cadet.search;

import java.util.List;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.search.SearchCapability;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public interface SearchProxyProvider extends Provider {

    /**
     * Get information about the search provider
     */
    public ServiceInfo about() throws TException;

    /**
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get a list of search type and language pairs for a search provider
     */
    List<SearchCapability> getCapabilities(String provider) throws ServicesException, TException;

    /**
     * Get a corpus list for a search provider
     */
    List<String> getCorpora(String provider) throws ServicesException, TException;

    /**
     * Get a list of search providers behind the proxy
     */
    public List<String> getProviders() throws ServicesException, TException;
    
    /**
     * Specify the search provider when performing a search
     */
    public SearchResults search(SearchQuery query, String provider) throws ServicesException, TException;    
}
