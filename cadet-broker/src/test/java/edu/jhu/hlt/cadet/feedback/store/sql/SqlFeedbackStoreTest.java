package edu.jhu.hlt.cadet.feedback.store.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhu.hlt.cadet.feedback.store.CommunicationFeedback;
import edu.jhu.hlt.cadet.feedback.store.FeedbackException;
import edu.jhu.hlt.cadet.feedback.store.FeedbackQuery;
import edu.jhu.hlt.cadet.feedback.store.FeedbackStore;
import edu.jhu.hlt.cadet.feedback.store.SentenceFeedback;
import edu.jhu.hlt.cadet.feedback.store.SentenceIdentifier;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.search.SearchType;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class SqlFeedbackStoreTest {
    private FeedbackStore store;

    @Before
    public void setUp() throws Exception {
        Config config = ConfigFactory.parseFile(new File(getFilePath("feedback/sql.conf")));
        store = new SqlFeedbackStore();
        store.init(config);
    }

    @After
    public void tearDown() throws Exception {
        store.close();
    }

    @Test
    public void testSavingSearchResultWithCommunication() throws ConcreteException {
        SearchResult results = createSearchResult("test", "ted", "my_query", SearchType.COMMUNICATIONS, 2);

        store.addSearchResults(results);

        Set<CommunicationFeedback> data = store.getAllCommunicationFeedback();
        assertNotNull(data);
        assertEquals(1, data.size());
        CommunicationFeedback fb = data.iterator().next();
        SearchResult res = fb.getSearchResults();
        assertEquals(res.getUuid(), results.getUuid());
        assertEquals(res.getSearchQuery().getUserId(), "ted");
        assertEquals(res.getSearchResultItemsSize(), 2);
        assertEquals(res.getSearchResultItemsIterator().next().getScore(), results.getSearchResultItemsIterator().next().getScore(), 0.001);
        assertEquals(0, store.getAllSentenceFeedback().size());
    }

    @Test
    public void testSavingSearchResultWithSentences() throws ConcreteException {
        SearchResult results = createSearchResult("test", "ted", "my_query", SearchType.SENTENCES, 3);

        store.addSearchResults(results);

        Set<SentenceFeedback> data = store.getAllSentenceFeedback();
        assertNotNull(data);
        assertEquals(1, data.size());
        SentenceFeedback fb = data.iterator().next();
        SearchResult res = fb.getSearchResults();
        assertEquals(res.getUuid(), results.getUuid());
        assertEquals(res.getSearchQuery().getUserId(), "ted");
        assertEquals(res.getSearchResultItemsSize(), 3);
        assertEquals(res.getSearchResultItemsIterator().next().getScore(), results.getSearchResultItemsIterator().next().getScore(), 0.001);
        assertEquals(0, store.getAllCommunicationFeedback().size());
    }

    @Test
    public void testAddingCommunicationFeedback() throws ConcreteException, FeedbackException {
        SearchResult results = createSearchResult("test", "ted", "my_query", SearchType.COMMUNICATIONS, 4);
        store.addSearchResults(results);
        List<SearchResultItem> list = results.getSearchResultItems();
        store.addFeedback(results.getUuid(), list.get(0).getCommunicationId(), SearchFeedback.POSITIVE);
        store.addFeedback(results.getUuid(), list.get(1).getCommunicationId(), SearchFeedback.NONE);
        store.addFeedback(results.getUuid(), list.get(2).getCommunicationId(), SearchFeedback.NEGATIVE);

        assertEquals(0, store.getSentenceFeedback(results.getUuid()).size());
        Map<String, SearchFeedback> map = store.getCommunicationFeedback(results.getUuid());
        assertEquals(SearchFeedback.POSITIVE, map.get(list.get(0).getCommunicationId()));
        assertEquals(SearchFeedback.NONE, map.get(list.get(1).getCommunicationId()));
        assertEquals(SearchFeedback.NEGATIVE, map.get(list.get(2).getCommunicationId()));
        assertEquals(SearchFeedback.NONE, map.get(list.get(3).getCommunicationId()));
    }

    @Test
    public void testAddingSentenceFeedback() throws ConcreteException, FeedbackException {
        SearchResult results = createSearchResult("test", "ted", "my_query", SearchType.SENTENCES, 4);
        store.addSearchResults(results);
        List<SearchResultItem> list = results.getSearchResultItems();
        store.addFeedback(results.getUuid(), list.get(0).getCommunicationId(), list.get(0).getSentenceId(), SearchFeedback.POSITIVE);
        store.addFeedback(results.getUuid(), list.get(1).getCommunicationId(), list.get(1).getSentenceId(), SearchFeedback.NONE);
        store.addFeedback(results.getUuid(), list.get(2).getCommunicationId(), list.get(2).getSentenceId(), SearchFeedback.NEGATIVE);

        assertEquals(0, store.getCommunicationFeedback(results.getUuid()).size());
        Map<SentenceIdentifier, SearchFeedback> map = store.getSentenceFeedback(results.getUuid());
        assertEquals(SearchFeedback.POSITIVE, map.get(new SentenceIdentifier(list.get(0).getCommunicationId(), list.get(0).getSentenceId())));
        assertEquals(SearchFeedback.NONE, map.get(new SentenceIdentifier(list.get(1).getCommunicationId(), list.get(1).getSentenceId())));
        assertEquals(SearchFeedback.NEGATIVE, map.get(new SentenceIdentifier(list.get(2).getCommunicationId(), list.get(2).getSentenceId())));
        assertEquals(SearchFeedback.NONE, map.get(new SentenceIdentifier(list.get(3).getCommunicationId(), list.get(3).getSentenceId())));
    }

    @Test
    public void testAddingFeedbackToUnknownResult() {
        try {
            store.addFeedback(new UUID("unknown"), "testing", SearchFeedback.POSITIVE);
            fail("Did not get exception when adding feedback to unknown result");
        } catch (FeedbackException e) {}
    }

    // hacky attempt at verifying the store works with threads
    @Test(timeout=1000)
    public void testAddFeedbackThreaded() throws InterruptedException {
        List<Thread> threads = new ArrayList<Thread>();
        List<Client> clients = new ArrayList<Client>();

        Object syncObject = new Object();
        for (int i=0; i<10; i++) {
            Client client = new Client("client" + i, syncObject, store);
            Thread thread = new Thread(client);
            thread.start();
            threads.add(thread);
            clients.add(client);
        }

        // make sure threads are ready
        for (Client client : clients) {
            while (!client.isReady()) {
                Thread.sleep(1);
            }
        }

        // tell the threads to start pushing data to the store
        synchronized(syncObject) {
            syncObject.notifyAll();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (Client client : clients) {
            if (client.isFail()) {
                fail("There was a failure in one of the clients");
            }
        }

        Set<CommunicationFeedback> fb = store.getAllCommunicationFeedback();
        assertEquals(10, fb.size());
        for (CommunicationFeedback cf : fb) {
            assertEquals(1, Collections.frequency(cf.getFeedback().values(), SearchFeedback.POSITIVE));
            assertEquals(2, Collections.frequency(cf.getFeedback().values(), SearchFeedback.NONE));
            assertEquals(1, Collections.frequency(cf.getFeedback().values(), SearchFeedback.NEGATIVE));
        }
    }

    @Test
    public void testQueryCommunicationFeedback() throws ConcreteException {
        loadLotsOfData(SearchType.COMMUNICATIONS);

        // test limit
        FeedbackQuery q1 = new FeedbackQuery();
        q1.setLimit(3);
        assertEquals(3, store.queryCommunicationFeedback(q1).size());

        // test single user
        FeedbackQuery q2 = new FeedbackQuery();
        q2.setUserName("ed");
        assertEquals(2, store.queryCommunicationFeedback(q2).size());

        // test multiple users
        FeedbackQuery q3 = new FeedbackQuery();
        q3.setUserNames(new String[]{"bob", "ed"});
        assertEquals(5, store.queryCommunicationFeedback(q3).size());

        // test single query name
        FeedbackQuery q4 = new FeedbackQuery();
        q4.setQueryName("south");
        assertEquals(1, store.queryCommunicationFeedback(q4).size());
    }

    @Test
    public void testQuerySentenceFeedback() throws ConcreteException {
        loadLotsOfData(SearchType.SENTENCES);

        // test no limit
        FeedbackQuery q1 = new FeedbackQuery();
        assertEquals(6, store.querySentenceFeedback(q1).size());

        // test query name and user
        FeedbackQuery q2 = new FeedbackQuery();
        q2.setQueryNames(new String[]{"east", "south"});
        q2.setUserNames(new String[]{"ed", "greg"});
        assertEquals(2, store.querySentenceFeedback(q2).size());

        // test single label
        FeedbackQuery q3 = new FeedbackQuery();
        q3.setLabels(new String[]{"red"});
        assertEquals(2, store.querySentenceFeedback(q3).size());

        // test single label
        FeedbackQuery q4 = new FeedbackQuery();
        q4.setLabels(new String[]{"blue", "pink", "orange"});
        assertEquals(2, store.querySentenceFeedback(q4).size());
    }

    private String getFilePath(String filename) {
        ClassLoader classLoader = SqlFeedbackStoreTest.class.getClassLoader();
        java.net.URL url = classLoader.getResource(filename);
        try {
            Path path = Paths.get(url.toURI());
            return path.toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SearchResult createSearchResult(String id, String user, String name, SearchType type, int numResults) {
        SearchQuery q = new SearchQuery();
        q.setUserId(user);
        q.setName(name);
        q.setRawQuery("where?");
        q.setType(type);
        SearchResult results = new SearchResult(new UUID(id), q);
        for (int i=1; i<=numResults; i++) {
            SearchResultItem r = new SearchResultItem();
            r.setCommunicationId("doc" + i);
            r.setScore(Math.random());
            if (type == SearchType.SENTENCES) {
                r.setSentenceId(new UUID("sent" + i));
            }
            results.addToSearchResultItems(r);
        }
        return results;
    }

    private void loadLotsOfData(SearchType type) throws ConcreteException {
        SearchResult r1 = createSearchResult("c1", "bob", "east", type, 3);
        r1.getSearchQuery().addToLabels("red");
        store.addSearchResults(r1);
        SearchResult r2 = createSearchResult("c2", "bob", "east", type, 3);
        r2.getSearchQuery().addToLabels("blue");
        store.addSearchResults(r2);
        SearchResult r3 = createSearchResult("c3", "bob", "west", type, 3);
        r3.getSearchQuery().addToLabels("green");
        store.addSearchResults(r3);
        SearchResult r4 = createSearchResult("c4", "ed", "south", type, 3);
        r4.getSearchQuery().addToLabels("yellow");
        store.addSearchResults(r4);
        SearchResult r5 = createSearchResult("c5", "ed", "north", type, 3);
        r5.getSearchQuery().addToLabels("purple");
        store.addSearchResults(r5);
        SearchResult r6 = createSearchResult("c6", "greg", "east", type, 3);
        r6.getSearchQuery().addToLabels("orange");
        r6.getSearchQuery().addToLabels("red");
        store.addSearchResults(r6);
    }

    private class Client implements Runnable {
        private Object sync;
        private FeedbackStore store;
        private SearchResult results;
        private boolean fail = false;
        private boolean ready = false;

        public Client(String id, Object sync, FeedbackStore store) {
            this.sync = sync;
            this.store = store;
            results = createSearchResult(id, "larry", "my_query", SearchType.COMMUNICATIONS, 4);
        }

        public boolean isFail() {
            return fail;
        }

        public boolean isReady() {
            return ready;
        }

        @Override
        public void run() {
            synchronized(sync) {
                try {
                    ready = true;
                    sync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                store.addSearchResults(results);
            } catch (ConcreteException e) {
                fail = true;
                e.printStackTrace();
                return;
            }

            try {
                store.addFeedback(results.getUuid(), results.getSearchResultItems().get(0).getCommunicationId(), SearchFeedback.POSITIVE);
                store.addFeedback(results.getUuid(), results.getSearchResultItems().get(1).getCommunicationId(), SearchFeedback.NEGATIVE);
            } catch (FeedbackException e) {
                fail = true;
                e.printStackTrace();
                return;
            }
        }
    }

}
