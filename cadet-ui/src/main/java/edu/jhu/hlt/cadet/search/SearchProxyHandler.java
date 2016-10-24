package edu.jhu.hlt.cadet.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.search.SearchCapability;
import edu.jhu.hlt.concrete.search.SearchProxyService;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public class SearchProxyHandler implements SearchProxyService.Iface {
    Map<String, SearchProvider> providerMap = new HashMap<String, SearchProvider>();

    public void addProvider(String providerName, SearchProvider provider) {
        providerMap.put(providerName, provider);
    }

    @Override
    public ServiceInfo about() throws TException {
        ServiceInfo si = new ServiceInfo("SearchProxyHandler", "v1.0.0");
        return si;
    }

    @Override
    public boolean alive() throws TException {
        return true;
    }

    @Override
    public List<SearchCapability> getCapabilities(String provider)
            throws ServicesException, TException {
        return providerMap.get(provider).getCapabilities();
    }

    @Override
    public List<String> getCorpora(String providerName)
            throws ServicesException, TException {
        SearchProvider searchProvider = providerMap.get(providerName);
        if (searchProvider != null) {
            return searchProvider.getCorpora();
        }
        else {
            throw new ServicesException("Unable to find configuration for SearchProvider named " + providerName);
        }
    }

    @Override
    public List<String> getProviders() throws ServicesException, TException {
        List<String> providers = new ArrayList<String>(providerMap.keySet());
        return providers;
    }

    @Override
    public SearchResult search(SearchQuery query, String providerName)
            throws ServicesException, TException {
        SearchProvider searchProvider = providerMap.get(providerName);
        if (searchProvider != null) {
            return searchProvider.search(query);
        }
        else {
            throw new ServicesException("Unable to find configuration for SearchProvider named " + providerName);
        }
    }
}
