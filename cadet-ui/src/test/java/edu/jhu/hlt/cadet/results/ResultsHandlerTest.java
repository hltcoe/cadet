package edu.jhu.hlt.cadet.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.apache.thrift.TException;
import org.junit.Test;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class ResultsHandlerTest {

    protected ResultsHandler getHandler() {
        ResultsHandler handler = new ResultsHandler();
        handler.setResultsStore(new MemoryResultsStore());
        handler.setSessionStore(new MemorySessionStore());
        return handler;
    }

    @Test
    public void testValidation() {
        SearchQuery q = new SearchQuery();
        SearchResult r = new SearchResult();
        r.setUuid(new UUID("test"));
        ResultsHandler handler = new ResultsHandler();

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
        ResultsHandler handler = new ResultsHandler();
        ResultsStore store = new MemoryResultsStore();
        handler.setResultsStore(store);

        SearchQuery q = new SearchQuery();
        q.setRawQuery("what time is it ?");
        SearchResult r = new SearchResult(new UUID("test"), q);
        r.setSearchResultItems(new ArrayList<>());

        handler.registerSearchResult(r, AnnotationTaskType.NER);

        assertEquals("what time is it ?", store.getByID(new UUID("test")).getSearchQuery().getName());
    }

    @Test
    public void testRegisteringSearchResultWithName() throws ServicesException, TException {
        ResultsHandler handler = new ResultsHandler();
        ResultsStore store = new MemoryResultsStore();
        handler.setResultsStore(store);

        SearchQuery q = new SearchQuery();
        q.setRawQuery("what time is it ?");
        q.setName("time");
        SearchResult r = new SearchResult(new UUID("test"), q);
        r.setSearchResultItems(new ArrayList<>());

        handler.registerSearchResult(r, AnnotationTaskType.NER);

        assertEquals("time", store.getByID(new UUID("test")).getSearchQuery().getName());
    }

    @Test(expected=ServicesException.class)
    public void testStartSessionWithBadSearchResults() throws ServicesException, TException {
        ResultsHandler handler = getHandler();

        handler.startSession(new UUID("does not exist"), AnnotationTaskType.NER);
    }

    @Test(expected=ServicesException.class)
    public void testGetNextChunkWithBadSession() throws ServicesException, TException {
        ResultsHandler handler = getHandler();

        handler.getNextChunk(new UUID("does not exist"));
    }

    @Test(expected=ServicesException.class)
    public void testSubmitAnnotationWithBadSession() throws ServicesException, TException {
        ResultsHandler handler = getHandler();

        handler.submitAnnotation(new UUID("does not exist"),
                        new AnnotationUnitIdentifier(), new Communication());
    }

    @Test
    public void testPluginFiltering() throws ServicesException, TException {
        ResultsHandler handler = new ResultsHandler();
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
        public boolean process(SearchResult results) {
            return false;
        }
    }

}
