package edu.jhu.hlt.cadet.search;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;

import java.util.ArrayList;

import org.apache.thrift.TException;
import org.junit.Test;

import edu.jhu.hlt.concrete.search.SearchQuery;

public class SearchHandlerTest {

    @Test
    public void testForEmptySearchQuery() {
        SearchHandler handler = new SearchHandler();
        SearchQuery query = new SearchQuery();
        query.setTerms(new ArrayList<String>());
        query.setQuestions(null);
        query.setRawQuery("");

        try {
            handler.search(query);
            fail("Exception was not thrown with empty query");
        } catch (TException e) {
            assertThat(e.getMessage(), is("Search query is empty"));
        }
    }

}
