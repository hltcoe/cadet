package edu.jhu.hlt.cadet.results;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;

public class AnnotationSessionTest {

    private AnnotationSession session;

    @Before
    public void setUp() {
        SearchQuery query = new SearchQuery();
        SearchResults results = new SearchResults(new UUID("test"), query);
        results.addToSearchResults(createItem("1"));
        results.addToSearchResults(createItem("2"));
        results.addToSearchResults(createItem("3"));
        results.addToSearchResults(createItem("4"));
        results.addToSearchResults(createItem("5"));
        results.addToSearchResults(createItem("6"));
        results.addToSearchResults(createItem("7"));
        session = new AnnotationSession(results);
    }

    private SearchResult createItem(String id) {
        SearchResult r = new SearchResult();
        r.setCommunicationId(id);
        r.setSentenceId(new UUID(id + "_sentence"));
        return r;
    }

    private AnnotationUnitIdentifier createAUI(String id) {
        return AnnotationSession.createAUI(id, new UUID(id + "_sentence"));
    }

    @Test
    public void testGetNext() {
        List<AnnotationUnitIdentifier> data = session.getNext(2);
        assertEquals(2, data.size());
        assertEquals("1", data.get(0).getCommunicationId());
        assertEquals("2", data.get(1).getCommunicationId());

        data = session.getNext(2);
        assertEquals(2, data.size());
        assertEquals("3", data.get(0).getCommunicationId());
        assertEquals("4", data.get(1).getCommunicationId());

        data = session.getNext(2);
        data = session.getNext(2);
        assertEquals(1, data.size());
        assertEquals("7", data.get(0).getCommunicationId());
    }

    @Test
    public void testUpdateSort() {
        List<AnnotationUnitIdentifier> newSort = new ArrayList<>();
        newSort.add(createAUI("5"));
        newSort.add(createAUI("2"));
        newSort.add(createAUI("7"));
        newSort.add(createAUI("4"));
        newSort.add(createAUI("1"));
        newSort.add(createAUI("6"));
        newSort.add(createAUI("3"));

        assertTrue(session.updateSort(newSort));

        List<AnnotationUnitIdentifier> data = session.getNext(3);
        assertEquals(3, data.size());
        assertEquals("5", data.get(0).getCommunicationId());
        assertEquals("2", data.get(1).getCommunicationId());
        assertEquals("7", data.get(2).getCommunicationId());
    }

    @Test
    public void testBadUpdateSort() {
        List<AnnotationUnitIdentifier> newSort = new ArrayList<>();
        newSort.add(createAUI("1"));
        newSort.add(createAUI("2"));

        assertFalse(session.updateSort(newSort));
    }

    @Test
    public void testUpdateSortAfterFirstChunk() {
        List<AnnotationUnitIdentifier> data = session.getNext(2);

        // not changing order but checking if equalsTo works
        List<AnnotationUnitIdentifier> newSort = new ArrayList<>();
        newSort.add(createAUI("1"));
        newSort.add(createAUI("2"));
        newSort.add(createAUI("3"));
        newSort.add(createAUI("4"));
        newSort.add(createAUI("5"));
        newSort.add(createAUI("6"));
        newSort.add(createAUI("7"));

        assertTrue(session.updateSort(newSort));

        data = session.getNext(2);
        assertEquals(2, data.size());
        assertEquals("3", data.get(0).getCommunicationId());
        assertEquals("4", data.get(1).getCommunicationId());
    }

}
