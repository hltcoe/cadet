package edu.jhu.hlt.cadet.feedback.store;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class CommunicationFeedbackTest {

    @Test
    public void testValidateWithGoodObject() {
        SearchResults sr = new SearchResults(new UUID("test"), new SearchQuery());
        SearchResult item = new SearchResult();
        item.setCommunicationId("nytimes_89");
        sr.addToSearchResults(item);

        try {
            @SuppressWarnings("unused")
            CommunicationFeedback cf = new CommunicationFeedback(sr);
        } catch (ConcreteException e) {
            fail("Failed on good creation: " + e.getMessage());
        }
    }

    @Test(expected = ConcreteException.class)
    public void testValidateWithBadObject() throws ConcreteException {
        SearchResults sr = new SearchResults(new UUID("test"), new SearchQuery());

        @SuppressWarnings("unused")
        CommunicationFeedback cf = new CommunicationFeedback(sr);
    }

    @Test(expected = ConcreteException.class)
    public void testValidateWithMissingCommId() throws ConcreteException {
        SearchResults sr = new SearchResults(new UUID("test"), new SearchQuery());
        SearchResult item = new SearchResult();
        sr.addToSearchResults(item);

        @SuppressWarnings("unused")
        CommunicationFeedback cf = new CommunicationFeedback(sr);
    }

    @Test
    public void testAddFeedback() throws ConcreteException {
        SearchResults sr = new SearchResults(new UUID("test"), new SearchQuery());
        SearchResult item1 = new SearchResult();
        item1.setCommunicationId("nytimes_89");
        sr.addToSearchResults(item1);
        SearchResult item2 = new SearchResult();
        item2.setCommunicationId("nytimes_12");
        sr.addToSearchResults(item2);
        CommunicationFeedback cf = new CommunicationFeedback(sr);

        cf.addFeedback("nytimes_89", SearchFeedback.NEGATIVE);

        Map<String, SearchFeedback> data = cf.getFeedback();
        assertEquals(2, data.size());
        assertEquals(SearchFeedback.NEGATIVE, data.get("nytimes_89"));
        assertEquals(SearchFeedback.NONE, data.get("nytimes_12"));
    }

    @Test
    public void testAddFeedbackWithUnknownId() throws ConcreteException {
        SearchResults sr = new SearchResults(new UUID("test"), new SearchQuery());
        SearchResult item = new SearchResult();
        item.setCommunicationId("nytimes_89");
        sr.addToSearchResults(item);
        CommunicationFeedback cf = new CommunicationFeedback(sr);

        assertFalse(cf.addFeedback("latimes_55", SearchFeedback.NEGATIVE));
    }
}
