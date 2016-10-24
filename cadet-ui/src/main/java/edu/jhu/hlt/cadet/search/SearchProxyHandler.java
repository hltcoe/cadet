package edu.jhu.hlt.cadet.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.search.SearchCapability;
import edu.jhu.hlt.concrete.search.SearchProxyService;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public class SearchProxyHandler implements SearchProxyService.Iface {
    private static Logger logger = LoggerFactory.getLogger(RemoteSearchProvider.class);

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
            logger.info("getCorpora() called for SearchProvider " + providerName);
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
        if (searchProvider == null) {
            throw new ServicesException("Unable to find configuration for SearchProvider named " + providerName);
        }

        logSearchQuery(query);

        validate(query);

        SearchResult result = searchProvider.search(query);

        logSearchResults(result);

        return result;
    }

    private void validate(SearchQuery query) throws ServicesException {
        if (query.getRawQuery() == null || query.getRawQuery().isEmpty()) {
            throw new ServicesException("Search query is empty");
        }
    }

    protected static void logSearchQuery(SearchQuery searchQuery) {
        logger.info("Search query: " + searchQuery.getRawQuery());

        Iterator<String> questionIterator = searchQuery.getQuestionsIterator();
        Iterator<String> termIterator = searchQuery.getTermsIterator();

        if (searchQuery.getTermsSize() == 0) {
            logger.debug("No Terms provided");
        } else {
            logger.debug(searchQuery.getTermsSize() + " Term(s) provided");
            while (termIterator.hasNext()) {
                logger.debug("Term: " + termIterator.next());
            }
        }

        if (searchQuery.getQuestionsSize() == 0) {
            logger.debug("No questions provided");
        } else {
            logger.debug(searchQuery.getQuestionsSize() + " question(s) provided");
            while (questionIterator.hasNext()) {
                logger.debug("Question: " + questionIterator.next());
            }
        }
    }

    protected static void logSearchResults(SearchResult searchResults) {
        Iterator<SearchResultItem> searchResultsIterator = searchResults.getSearchResultItemsIterator();

        if (searchResults.getSearchResultItemsSize() == 0) {
            logger.info("Search: No results returned");
        } else {
            logger.info("Search: " + searchResults.getSearchResultItemsSize() + " result(s) provided");
            while (searchResultsIterator.hasNext()) {
                logger.debug("SearchResult: " + searchResultsIterator.next());
            }
        }
    }
}
