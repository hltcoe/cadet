/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.feedback.store;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class CommunicationFeedbackTest {

    @Test
    public void testValidateWithGoodObject() {
        SearchResult sr = new SearchResult(new UUID("test"), new SearchQuery());
        SearchResultItem item = new SearchResultItem();
        item.setCommunicationId("nytimes_89");
        sr.addToSearchResultItems(item);

        try {
            @SuppressWarnings("unused")
            CommunicationFeedback cf = new CommunicationFeedback(sr);
        } catch (ConcreteException e) {
            fail("Failed on good creation: " + e.getMessage());
        }
    }

    @Test(expected = ConcreteException.class)
    public void testValidateWithBadObject() throws ConcreteException {
        SearchResult sr = new SearchResult(new UUID("test"), new SearchQuery());

        @SuppressWarnings("unused")
        CommunicationFeedback cf = new CommunicationFeedback(sr);
    }

    @Test(expected = ConcreteException.class)
    public void testValidateWithMissingCommId() throws ConcreteException {
        SearchResult sr = new SearchResult(new UUID("test"), new SearchQuery());
        SearchResultItem item = new SearchResultItem();
        sr.addToSearchResultItems(item);

        @SuppressWarnings("unused")
        CommunicationFeedback cf = new CommunicationFeedback(sr);
    }

    @Test
    public void testAddFeedback() throws ConcreteException {
        SearchResult sr = new SearchResult(new UUID("test"), new SearchQuery());
        SearchResultItem item1 = new SearchResultItem();
        item1.setCommunicationId("nytimes_89");
        sr.addToSearchResultItems(item1);
        SearchResultItem item2 = new SearchResultItem();
        item2.setCommunicationId("nytimes_12");
        sr.addToSearchResultItems(item2);
        CommunicationFeedback cf = new CommunicationFeedback(sr);

        cf.addFeedback("nytimes_89", SearchFeedback.NEGATIVE);

        Map<String, SearchFeedback> data = cf.getFeedback();
        assertEquals(2, data.size());
        assertEquals(SearchFeedback.NEGATIVE, data.get("nytimes_89"));
        assertEquals(SearchFeedback.NONE, data.get("nytimes_12"));
    }

    @Test
    public void testAddFeedbackWithUnknownId() throws ConcreteException {
      SearchResult sr = new SearchResult(new UUID("test"), new SearchQuery());
        SearchResultItem item = new SearchResultItem();
        item.setCommunicationId("nytimes_89");
        sr.addToSearchResultItems(item);
        CommunicationFeedback cf = new CommunicationFeedback(sr);

        assertFalse(cf.addFeedback("latimes_55", SearchFeedback.NEGATIVE));
    }
}
