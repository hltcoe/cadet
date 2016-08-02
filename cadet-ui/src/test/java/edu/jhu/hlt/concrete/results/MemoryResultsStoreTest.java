package edu.jhu.hlt.concrete.results;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.ServicesException;

public class MemoryResultsStoreTest {
    private ResultsStore store;

    @Before
    public void setUp() throws InterruptedException, ServicesException {
        store = new MemoryResultsStore();

        SearchQuery q1 = new SearchQuery();
        q1.setUserId("bob");
        q1.setRawQuery("where is dc?");
        SearchResults r1 = new SearchResults(new UUID("test1"), q1);
        store.add(r1, AnnotationTaskType.NER);
        Thread.sleep(0, 1);

        SearchQuery q2 = new SearchQuery();
        q2.setUserId("bob");
        q2.setRawQuery("what is blue?");
        SearchResults r2 = new SearchResults(new UUID("test2"), q2);
        store.add(r2, AnnotationTaskType.NER);
        Thread.sleep(0, 1);

        SearchQuery q3 = new SearchQuery();
        q3.setUserId("ed");
        q3.setRawQuery("what is red?");
        SearchResults r3 = new SearchResults(new UUID("test3"), q3);
        store.add(r3, AnnotationTaskType.NER);
        Thread.sleep(0, 1);

        SearchQuery q4 = new SearchQuery();
        q4.setUserId("ed");
        q4.setRawQuery("what is green?");
        SearchResults r4 = new SearchResults(new UUID("test4"), q4);
        store.add(r4, AnnotationTaskType.TRANSLATION);
    }

    @Test
    public void testGetById() {
        SearchResults r = store.getByID(new UUID("test1"));
        assertEquals("where is dc?", r.getSearchQuery().getRawQuery());

        assertNull(store.getByID(new UUID("nothing")));
    }

    @Test
    public void testGetLatest() {
        SearchResults r1 = store.getLatest("ed");
        assertEquals(new UUID("test4"), r1.getUuid());

        SearchResults r2 = store.getLatest("bob");
        assertEquals(new UUID("test2"), r2.getUuid());

        assertNull(store.getLatest("kevin"));
    }

    @Test
    public void testGetByTask() {
        List<SearchResults> list = store.getByTask(AnnotationTaskType.NER, 0);
        assertEquals(3, list.size());

        list = store.getByTask(AnnotationTaskType.NER, 2);
        assertEquals(2, list.size());
    }

    @Test
    public void testGetByUser() {
        List<SearchResults> list = store.getByUser(AnnotationTaskType.NER, "ed", 0);
        assertEquals(1, list.size());

        list = store.getByUser(AnnotationTaskType.NER, "bob", 1);
        assertEquals(1, list.size());        

        assertTrue(store.getByUser(AnnotationTaskType.TRANSLATION, "bob", 0).isEmpty());
    }

    @Test
    public void testAddTask() throws ServicesException {
        SearchQuery q = new SearchQuery();
        q.setUserId("ed");
        q.setRawQuery("what is green?");
        SearchResults r = new SearchResults(new UUID("test4"), q);
        store.add(r, AnnotationTaskType.NER);

        List<SearchResults> list = store.getByTask(AnnotationTaskType.NER, 0);
        assertEquals(4, list.size());
    }
}
