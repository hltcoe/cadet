package edu.jhu.hlt.cadet.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.search.SearchCapability;
import edu.jhu.hlt.concrete.search.SearchProxy;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

// TODO: SearchProxyHandler is currently a stub that uses a single SearchProvider,
//       and needs to be updated different search providers to be selected.

public class SearchProxyHandler implements SearchProxy.Iface {
    private SearchProvider searchProvider;

    public void init(SearchProvider provider) {
        searchProvider = provider;
    }

    @Override
    public ServiceInfo about() throws TException {
        return searchProvider.about();
    }

    @Override
    public boolean alive() throws TException {
        return searchProvider.alive();
    }

    @Override
    public List<SearchCapability> getCapabilities(String provider)
            throws ServicesException, TException {
        return searchProvider.getCapabilities();
    }

    @Override
    public List<String> getCorpora(String provider)
            throws ServicesException, TException {
        return searchProvider.getCorpora();
    }

    @Override
    public List<String> getProviders() throws ServicesException, TException {
        List<String> providers = new ArrayList<String>();
        providers.add("default");
        return providers;
    }

    @Override
    public SearchResults search(SearchQuery query, String provider)
            throws ServicesException, TException {
        return searchProvider.search(query);
    }
}
