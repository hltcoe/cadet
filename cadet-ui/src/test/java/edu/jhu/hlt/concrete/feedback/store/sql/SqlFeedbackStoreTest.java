package edu.jhu.hlt.concrete.feedback.store.sql;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.feedback.store.CommunicationFeedback;
import edu.jhu.hlt.concrete.feedback.store.FeedbackException;
import edu.jhu.hlt.concrete.feedback.store.FeedbackQuery;
import edu.jhu.hlt.concrete.feedback.store.FeedbackStore;
import edu.jhu.hlt.concrete.feedback.store.SentenceFeedback;
import edu.jhu.hlt.concrete.feedback.store.SentenceIdentifier;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;
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
        ((SqlFeedbackStore)store).close();
    }

    @Test
    public void testSavingSearchResultsWithCommunication() throws ConcreteException {
        SearchResults results = createSearchResults("test", "ted", "my_query", SearchType.COMMUNICATIONS, 2);

        store.addSearchResults(results);

        Set<CommunicationFeedback> data = store.getAllCommunicationFeedback();
        assertNotNull(data);
        assertEquals(1, data.size());
        CommunicationFeedback fb = data.iterator().next();
        SearchResults res = fb.getSearchResults();
        assertEquals(res.getUuid(), results.getUuid());
        assertEquals(res.getSearchQuery().getUserId(), "ted");
        assertEquals(res.getSearchResultsSize(), 2);
        assertEquals(res.getSearchResultsIterator().next().getScore(), results.getSearchResultsIterator().next().getScore(), 0.001);
        assertEquals(0, store.getAllSentenceFeedback().size());
    }

    @Test
    public void testSavingSearchResultsWithSentences() throws ConcreteException {
        SearchResults results = createSearchResults("test", "ted", "my_query", SearchType.SENTENCES, 3);

        store.addSearchResults(results);

        Set<SentenceFeedback> data = store.getAllSentenceFeedback();
        assertNotNull(data);
        assertEquals(1, data.size());
        SentenceFeedback fb = data.iterator().next();
        SearchResults res = fb.getSearchResults();
        assertEquals(res.getUuid(), results.getUuid());
        assertEquals(res.getSearchQuery().getUserId(), "ted");
        assertEquals(res.getSearchResultsSize(), 3);
        assertEquals(res.getSearchResultsIterator().next().getScore(), results.getSearchResultsIterator().next().getScore(), 0.001);
        assertEquals(0, store.getAllCommunicationFeedback().size());
    }

    @Test
    public void testAddingCommunicationFeedback() throws ConcreteException, FeedbackException {
        SearchResults results = createSearchResults("test", "ted", "my_query", SearchType.COMMUNICATIONS, 4);
        store.addSearchResults(results);
        List<SearchResult> list = results.getSearchResults();
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
        SearchResults results = createSearchResults("test", "ted", "my_query", SearchType.SENTENCES, 4);
        store.addSearchResults(results);
        List<SearchResult> list = results.getSearchResults();
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

    private SearchResults createSearchResults(String id, String user, String name, SearchType type, int numResults) {
        SearchQuery q = new SearchQuery();
        q.setUserId(user);
        q.setName(name);
        q.setRawQuery("where?");
        q.setType(type);
        SearchResults results = new SearchResults(new UUID(id), q);
        for (int i=1; i<=numResults; i++) {
            SearchResult r = new SearchResult();
            r.setCommunicationId("doc" + i);
            r.setScore(Math.random());
            if (type == SearchType.SENTENCES) {
                r.setSentenceId(new UUID("sent" + i));
            }
            results.addToSearchResults(r);
        }
        return results;
    }

    private void loadLotsOfData(SearchType type) throws ConcreteException {
        SearchResults r1 = createSearchResults("c1", "bob", "east", type, 3);
        r1.getSearchQuery().addToLabels("red");
        store.addSearchResults(r1);
        SearchResults r2 = createSearchResults("c2", "bob", "east", type, 3);
        r2.getSearchQuery().addToLabels("blue");
        store.addSearchResults(r2);
        SearchResults r3 = createSearchResults("c3", "bob", "west", type, 3);
        r3.getSearchQuery().addToLabels("green");
        store.addSearchResults(r3);
        SearchResults r4 = createSearchResults("c4", "ed", "south", type, 3);
        r4.getSearchQuery().addToLabels("yellow");
        store.addSearchResults(r4);
        SearchResults r5 = createSearchResults("c5", "ed", "north", type, 3);
        r5.getSearchQuery().addToLabels("purple");
        store.addSearchResults(r5);
        SearchResults r6 = createSearchResults("c6", "greg", "east", type, 3);
        r6.getSearchQuery().addToLabels("orange");
        r6.getSearchQuery().addToLabels("red");
        store.addSearchResults(r6);
    }
}
