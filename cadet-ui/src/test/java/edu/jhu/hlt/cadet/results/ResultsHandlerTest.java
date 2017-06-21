package edu.jhu.hlt.cadet.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.apache.thrift.TException;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhu.hlt.cadet.fetch.FetchProvider;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class ResultsHandlerTest {

    @Test
    public void testValidation() {
        SearchQuery q = new SearchQuery();
        SearchResult r = new SearchResult();
        r.setUuid(new UUID("test"));
        ResultsHandler handler = new ResultsHandler(ConfigFactory.empty());

        // must have a search query
        try {
            handler.validate(r);
            fail("Did not catch missing search query");
        } catch (ConcreteException e) {
            assertEquals("Search results needs a search query", e.getMessage());
        }

        r.setSearchQuery(q);
        // must have a list of search results
        try {
            handler.validate(r);
            fail("Did not catch missing search results list");
        } catch (ConcreteException e) {
            assertEquals("Search results list cannot be missing", e.getMessage());
        }

        r.setSearchResultItems(new ArrayList<>());
        // must have query text
        try {
            handler.validate(r);
            fail("Did not catch missing query text");
        } catch (ConcreteException e) {
            assertEquals("Search query cannot be empty", e.getMessage());
        }

        r.getSearchQuery().setRawQuery("where am I?");
        // this should be valid now
        try {
            handler.validate(r);
        } catch (ConcreteException e) {
            fail("Valid search results failed validation");
        }
    }

    @Test
    public void testRegisteringSearchResultWithNoName() throws ServicesException, TException {
        ResultsHandler handler = new ResultsHandler(ConfigFactory.empty());
        ResultsStore store = new MemoryResultsStore();
        handler.setResultsStore(store);

        SearchQuery q = new SearchQuery();
        q.setRawQuery("what time is it ?");
        SearchResult r = new SearchResult(new UUID("test"), q);
        r.setSearchResultItems(new ArrayList<>());

        handler.registerSearchResult(r, AnnotationTaskType.NER);

        assertEquals("what time is it ?", store.getByID(new UUID("test")).results.getSearchQuery().getName());
    }

    @Test
    public void testRegisteringSearchResultWithName() throws ServicesException, TException {
        ResultsHandler handler = new ResultsHandler(ConfigFactory.empty());
        ResultsStore store = new MemoryResultsStore();
        handler.setResultsStore(store);

        SearchQuery q = new SearchQuery();
        q.setRawQuery("what time is it ?");
        q.setName("time");
        SearchResult r = new SearchResult(new UUID("test"), q);
        r.setSearchResultItems(new ArrayList<>());

        handler.registerSearchResult(r, AnnotationTaskType.NER);

        assertEquals("time", store.getByID(new UUID("test")).results.getSearchQuery().getName());
    }

    @Test
    public void testPluginFiltering() throws ServicesException, TException {
        ResultsHandler handler = new ResultsHandler(ConfigFactory.empty());
        ResultsStore store = new MemoryResultsStore();
        handler.setResultsStore(store);
        handler.addPlugin(new NoFilter());

        SearchQuery q = new SearchQuery();
        q.setRawQuery("lox and bagel");
        SearchResult r = new SearchResult(new UUID("test"), q);
        r.setSearchResultItems(new ArrayList<>());

        handler.registerSearchResult(r, AnnotationTaskType.NER);

        assertNull(store.getByID(new UUID("test")));
    }

    private class NoFilter implements ResultsPlugin {
        @Override
        public void init(Config config) {}

        @Override
        public void close() {}

        @Override
        public void setFetchProvider(FetchProvider provider) {}

        @Override
        public boolean process(SearchResult results) {
            return false;
        }
    }

}
