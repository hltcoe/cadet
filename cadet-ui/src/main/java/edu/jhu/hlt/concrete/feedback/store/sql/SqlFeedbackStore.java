package edu.jhu.hlt.concrete.feedback.store.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.feedback.store.CommunicationFeedback;
import edu.jhu.hlt.concrete.feedback.store.FeedbackException;
import edu.jhu.hlt.concrete.feedback.store.FeedbackQuery;
import edu.jhu.hlt.concrete.feedback.store.FeedbackStore;
import edu.jhu.hlt.concrete.feedback.store.SentenceFeedback;
import edu.jhu.hlt.concrete.feedback.store.SentenceIdentifier;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class SqlFeedbackStore implements FeedbackStore {
    private static Logger logger = LoggerFactory.getLogger(SqlFeedbackStore.class);

    private SessionFactory sessionFactory;
    // TODO replace with LRU cache
    private Map<UUID, FeedbackRecord> cache = new HashMap<UUID, FeedbackRecord>();

    @Override
    public void init(Config config) {
        config = config.getConfig("servlets.feedback.hibernate");
        Configuration dbConfig = new Configuration()
                .setProperty("hibernate.dialect", config.getString("dialect"))
                .setProperty("hibernate.connection.driver_class", config.getString("connection.driver_class"))
                .setProperty("hibernate.connection.url", config.getString("connection.url"))
                .setProperty("hibernate.connection.username", config.getString("connection.username"))
                .setProperty("hibernate.connection.password", config.getString("connection.password"))
                .setProperty("hibernate.hbm2ddl.auto", "create")
                .addAnnotatedClass(Feedback.class)
                .addAnnotatedClass(FeedbackRecord.class);
        sessionFactory = dbConfig.buildSessionFactory();
    }

    @Override
    public void addSearchResults(SearchResults results) throws ConcreteException {
        FeedbackRecord record = FeedbackRecord.create(results);
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(record);
        session.getTransaction().commit();
        session.close();
        cache.put(results.getUuid(), record);
        logger.debug("SearchResults stored in db");
    }

    @Override
    public void addFeedback(UUID uuid, String communicationId, SearchFeedback feedback)
                    throws FeedbackException {
        addFeedback(uuid, communicationId, null, feedback);
    }

    @Override
    public void addFeedback(UUID uuid, String communicationId, UUID sentenceId,
                    SearchFeedback feedback) throws FeedbackException {
        String sentIdString = null;
        if (sentenceId != null) {
            sentIdString = sentenceId.getUuidString();
        }
        FeedbackRecord record = cache.get(uuid);
        Feedback item = record.getFeedbackItem(communicationId, sentIdString);
        item.setValue(feedback);
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.update(item);
        session.getTransaction().commit();
        session.close();
        logger.debug("Feedback updated in db");
    }

    @Override
    public Map<String, SearchFeedback> getCommunicationFeedback(UUID uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<SentenceIdentifier, SearchFeedback> getSentenceFeedback(UUID uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<CommunicationFeedback> queryCommunicationFeedback(FeedbackQuery query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SentenceFeedback> querySentenceFeedback(FeedbackQuery query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<CommunicationFeedback> getAllCommunicationFeedback() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<SentenceFeedback> getAllSentenceFeedback() {
        // TODO Auto-generated method stub
        return null;
    }

}
