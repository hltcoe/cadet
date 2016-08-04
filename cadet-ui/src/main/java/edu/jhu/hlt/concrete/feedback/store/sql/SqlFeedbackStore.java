package edu.jhu.hlt.concrete.feedback.store.sql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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
import edu.jhu.hlt.concrete.search.SearchType;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class SqlFeedbackStore implements FeedbackStore {
    private static Logger logger = LoggerFactory.getLogger(SqlFeedbackStore.class);

    private SessionFactory sessionFactory;
    // TODO replace with LRU cache
    private Map<UUID, FeedbackRecord> cache = new HashMap<UUID, FeedbackRecord>();

    @Override
    public void init(Config config) {
        config = config.getConfig("servlets.feedback");
        Configuration dbConfig = new Configuration()
                .setProperty("hibernate.dialect", config.getString("hibernate.dialect"))
                .setProperty("hibernate.connection.driver_class", config.getString("hibernate.connection.driver_class"))
                .setProperty("hibernate.connection.url", config.getString("hibernate.connection.url"))
                .addAnnotatedClass(Feedback.class)
                .addAnnotatedClass(FeedbackRecord.class);
        updateConfig(dbConfig, config, "hibernate.connection.username");
        updateConfig(dbConfig, config, "hibernate.connection.password");
        updateConfig(dbConfig, config, "hibernate.hbm2ddl.auto", "validate");
        sessionFactory = dbConfig.buildSessionFactory();
    }

    private void updateConfig(Configuration dbConfig, Config config, String option) {
        updateConfig(dbConfig, config, option, null);
    }

    private void updateConfig(Configuration dbConfig, Config config, String option, String fallback) {
        if (config.hasPath(option)) {
            dbConfig.setProperty(option, config.getString(option));
        } else if (fallback != null) {
            dbConfig.setProperty(option, fallback);
        }
    }

    public void close() {
        sessionFactory.close();
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
        Map<String, SearchFeedback> map = new HashMap<>();

        FeedbackRecord record = getFeedbackRecord(uuid, SearchType.COMMUNICATIONS);
        if (record != null) {
            for (Feedback feedback : record.getFeedback()) {
                map.put(feedback.getCommId(), feedback.getValue());
            }
        }

        return map;
    }

    @Override
    public Map<SentenceIdentifier, SearchFeedback> getSentenceFeedback(UUID uuid) {
        Map<SentenceIdentifier, SearchFeedback> map = new HashMap<>();

        FeedbackRecord record = getFeedbackRecord(uuid, SearchType.SENTENCES);
        if (record != null) {
            for (Feedback fb : record.getFeedback()) {
                SentenceIdentifier id = new SentenceIdentifier(fb.getCommId(), new UUID(fb.getSentId()));
                map.put(id, fb.getValue());
            }
        }

        return map;
    }

    private FeedbackRecord getFeedbackRecord(UUID uuid, SearchType searchType) {
        Session session = sessionFactory.openSession();
        Transaction trans = session.beginTransaction();
        Query query = session.createQuery("from FeedbackRecord where uuid = :uuid and searchType = :type");
        query.setParameter("uuid", uuid.getUuidString());
        query.setParameter("type", searchType);
        FeedbackRecord record = (FeedbackRecord) query.uniqueResult();
        trans.commit();
        session.close();
        return record;
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
        Session session = sessionFactory.openSession();
        Transaction trans = session.beginTransaction();
        List<FeedbackRecord> records = getAllRecords(session, SearchType.COMMUNICATIONS);
        Set<CommunicationFeedback> data = new HashSet<CommunicationFeedback>();
        for (FeedbackRecord record : records) {
            try {
                CommunicationFeedback cf = new CommunicationFeedback(record.getSearchResults());
                for (Feedback fb : record.getFeedback()) {
                    cf.addFeedback(fb.getCommId(), fb.getValue());
                }
                data.add(cf);
            } catch (ConcreteException | FeedbackException e) {
                // likely deserializing old data - log and pass back empty set
                logger.error("Likely deserializing incompatible thrift object for feedback", e);
            }
        }

        trans.commit();
        session.close();

        return data;
    }

    @Override
    public Set<SentenceFeedback> getAllSentenceFeedback() {
        Session session = sessionFactory.openSession();
        Transaction trans = session.beginTransaction();
        List<FeedbackRecord> records = getAllRecords(session, SearchType.SENTENCES);
        Set<SentenceFeedback> data = new HashSet<SentenceFeedback>();
        for (FeedbackRecord record : records) {
            try {
                SentenceFeedback cf = new SentenceFeedback(record.getSearchResults());
                for (Feedback fb : record.getFeedback()) {
                    cf.addFeedback(fb.getCommId(), new UUID(fb.getSentId()), fb.getValue());
                }
                data.add(cf);
            } catch (ConcreteException | FeedbackException e) {
                // likely deserializing old data - log and pass back empty set
                logger.error("Likely deserializing incompatible thrift object for feedback", e);
            }
        }

        trans.commit();
        session.close();

        return data;
    }

    private List<FeedbackRecord> getAllRecords(Session session, SearchType type) {
        Query query = session.createQuery("from FeedbackRecord where searchType = :value");
        query.setParameter("value", type);
        return query.list();
    }

}
