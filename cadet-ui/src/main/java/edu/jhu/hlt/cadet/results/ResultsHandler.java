package edu.jhu.hlt.cadet.results;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.hlt.cadet.learn.ActiveLearningClient;
import edu.jhu.hlt.cadet.learn.SortReceiverCallback;
import edu.jhu.hlt.cadet.results.ResultsStore.Item;
import edu.jhu.hlt.cadet.store.StoreProvider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.Annotation;
import edu.jhu.hlt.concrete.learn.AnnotationTask;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.search.SearchType;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;
import edu.jhu.hlt.concrete.services.AnnotationUnitType;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.services.results.ResultsServerService;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class ResultsHandler implements ResultsServerService.Iface, SortReceiverCallback {
    private static Logger logger = LoggerFactory.getLogger(ResultsHandler.class);

    private final int chunkSize = 10;
    private ResultsStore resultsStore;
    private SessionStore sessionStore;
    private StoreProvider storeProvider;
    private ActiveLearningClient client;
    private List<ResultsPlugin> plugins = new ArrayList<ResultsPlugin>();

    public ResultsHandler() {}

    public void setResultsStore(ResultsStore store) {
        resultsStore = store;
    }

    public ResultsStore getResultsStore() {
        return resultsStore;
    }

    public void setSessionStore(SessionStore store) {
        sessionStore = store;
    }

    public SessionStore getSessionStore() {
        return sessionStore;
    }

    public void setStoreProvider(StoreProvider provider) {
        storeProvider = provider;
    }

    public StoreProvider getStoreProvider() {
        return storeProvider;
    }

    public void setActiveLearningClient(ActiveLearningClient provider) {
        client = provider;
    }

    public ActiveLearningClient getActiveLearningClient() {
        return client;
    }

    public void addPlugin(ResultsPlugin plugin) {
        plugins.add(plugin);
    }

    @Override
    public void registerSearchResult(SearchResult results, AnnotationTaskType taskType)
                    throws ServicesException, TException {
        logger.info("Results Server: registering search results "
                        + results.getUuid().getUuidString() + " for " + taskType.name());

        try {
            validate(results);
        } catch (ConcreteException e) {
            throw new ServicesException(e.getMessage());
        }

        boolean storeFlag = true;
        for (ResultsPlugin plugin : plugins) {
            storeFlag = storeFlag && plugin.process(results);
        }

        // we require a name for the SearchQuery for displaying to users
        if (!results.getSearchQuery().isSetName()) {
            results.getSearchQuery().setName(results.getSearchQuery().getRawQuery());
        }

        if (storeFlag) {
            resultsStore.add(results, taskType);
        }
    }

    @Override
    public List<SearchResult> getSearchResults(AnnotationTaskType taskType, int limit)
                    throws ServicesException, TException {
        return resultsStore.getByTask(taskType, limit);
    }

    @Override
    public List<SearchResult> getSearchResultsByUser(AnnotationTaskType taskType, String userId,
                    int limit) throws ServicesException, TException {
        return resultsStore.getByUser(taskType, userId, limit);
    }

    @Override
    public SearchResult getLatestSearchResult(String userId) throws ServicesException, TException {
        Item item = resultsStore.getLatest(userId);
        if (item != null) {
            return item.results;
        }
        return null;
    }

    @Override
    public SearchResult getSearchResult(UUID searchResultsId) throws ServicesException, TException {
        logger.info("Results server: fetching result " + searchResultsId.getUuidString());
        Item item = resultsStore.getByID(searchResultsId);
        if (item != null) {
            return item.results;
        }
        return null;
    }

    protected void validate(SearchResult results) throws ConcreteException {
        if (!results.isSetSearchQuery()) {
            throw new ConcreteException("Search results needs a search query");
        }
        if (!results.isSetSearchResultItems()) {
            throw new ConcreteException("Search results list cannot be missing");
        }
        if (!results.getSearchQuery().isSetRawQuery()) {
            throw new ConcreteException("Search query cannot be empty");
        }
    }

    private AnnotationUnitType convert(SearchType type) {
        AnnotationUnitType rtnType;
        switch (type) {
            case SENTENCES:
                rtnType = AnnotationUnitType.SENTENCE;
                break;
            case COMMUNICATIONS:
                rtnType = AnnotationUnitType.COMMUNICATION;
                break;
            default:
                rtnType = AnnotationUnitType.COMMUNICATION;
                break;
        }
        return rtnType;
    }

    private List<AnnotationUnitIdentifier> createList(List<SearchResultItem> results, boolean sentences) {
        List<AnnotationUnitIdentifier> list = new ArrayList<>();
        for (SearchResultItem result : results) {
            AnnotationUnitIdentifier id = new AnnotationUnitIdentifier(result.getCommunicationId());
            if (sentences) {
                id.setSentenceId(result.getSentenceId());
            }
            list.add(id);
        }
        return list;
    }

    @Override
    public UUID startSession(UUID searchResultsId, AnnotationTaskType taskType) throws ServicesException, TException {
        Item item = resultsStore.getByID(searchResultsId);
        if (item == null) {
            throw new ServicesException("Unknown search result id: " + searchResultsId.getUuidString());
        }

        SearchResult searchResult = item.results;
        AnnotationSession session = new AnnotationSession(searchResult);
        sessionStore.add(session);

        logger.info("Results server: starting annotation session on "
                        + searchResultsId.getUuidString() + " with session id "
                        + session.getId().getUuidString());
        if (client != null) {
            // priority given to task type submitted with startSession()
            if (taskType == null) {
                if (!item.tasks.isEmpty()) {
                    taskType = item.tasks.iterator().next();
                } else {
                    taskType = AnnotationTaskType.NER;
                }
            }

            String lang = "eng";
            if (searchResult.isSetLang()) {
                lang = searchResult.getLang();
            }

            AnnotationUnitType annType = convert(searchResult.getSearchQuery().getType());
            List<AnnotationUnitIdentifier> list = createList(searchResult.getSearchResultItems(),
                            annType == AnnotationUnitType.SENTENCE);
            AnnotationTask task = new AnnotationTask(taskType, annType, list);
            task.setLanguage(lang);
            logger.info("Task sending to active learning " + lang + taskType.name());
            client.start(session.getId(), task);
        }

        return session.getId();
    }

    @Override
    public void stopSession(UUID sessionId) throws ServicesException, TException {
        logger.info("Results server: stopping session " + sessionId.getUuidString());
        sessionStore.remove(sessionId);
        if (client != null) {
            client.stop(sessionId);
        }
    }

    @Override
    public List<AnnotationUnitIdentifier> getNextChunk(UUID sessionId) throws ServicesException, TException {
        logger.info("Results server: getting next chunk of data for session " + sessionId.getUuidString());
        AnnotationSession session = sessionStore.get(sessionId);
        return session.getNext(chunkSize);
    }

    @Override
    public void submitAnnotation(UUID sessionId, AnnotationUnitIdentifier unitId,
                    Communication communication) throws ServicesException, TException {
        logger.info("Results server: received annotation on session " + sessionId.getUuidString());
        AnnotationSession session = sessionStore.get(sessionId);
        session.addAnnotation(unitId, communication);

        if (client != null) {
            List<Annotation> anns = new ArrayList<Annotation>();
            anns.add(new Annotation(unitId, communication));
            client.addAnnotations(sessionId, anns);
        }

        try {
            storeProvider.store(communication);
        } catch (TException e) {
            throw new ServicesException("Unable to save the annotated communication");
        }
    }

    public List<AnnotationSession> getActiveSessions() {
        return sessionStore.list();
    }

    @Override
    public void addSort(UUID sessionId, List<AnnotationUnitIdentifier> unitIds) {
        logger.info("Results server: new sort received for session " + sessionId);

        AnnotationSession session = sessionStore.get(sessionId);
        if (session == null) {
            logger.warn("Received invalid session id from active learner: " + sessionId.getUuidString());
            return;
        }

        if (!session.updateSort(unitIds)) {
            logger.warn("Updated list from active learner was rejected");
        }
    }

    @Override
    public ServiceInfo about() throws TException {
        return new ServiceInfo("ResultsServer storing to " + resultsStore.getClass().getSimpleName(), "1.0.0");
    }

    @Override
    public boolean alive() throws TException {
        boolean storeAlive = storeProvider.alive();
        return storeAlive;
    }

}
