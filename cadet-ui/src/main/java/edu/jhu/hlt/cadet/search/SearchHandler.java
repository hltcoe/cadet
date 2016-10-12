package edu.jhu.hlt.cadet.search;

import java.util.Iterator;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.hlt.concrete.search.SearchService;
import edu.jhu.hlt.concrete.search.SearchCapability;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public class SearchHandler implements SearchService.Iface {
    private static Logger logger = LoggerFactory.getLogger(SearchHandler.class);

    private SearchProvider searchProvider;

    public SearchHandler() {
    }

    public void init(SearchProvider provider) {
        searchProvider = provider;
    }

    @Override
    public SearchResult search(SearchQuery searchQuery) throws ServicesException, TException {

        logSearchQuery(searchQuery);

        validate(searchQuery);

        SearchResult results = searchProvider.search(searchQuery);

        logSearchResults(results);

        return results;
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

    @Override
    public ServiceInfo about() throws TException {
        return searchProvider.about();
    }

    @Override
    public boolean alive() throws TException {
        return searchProvider.alive();
    }

    @Override
    public List<SearchCapability> getCapabilities() throws ServicesException, TException {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<String> getCorpora() throws ServicesException, TException {
      throw new UnsupportedOperationException("Not yet implemented");
    }

}
