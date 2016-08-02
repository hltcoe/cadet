package edu.jhu.hlt.cadet;

import edu.jhu.hlt.cadet.learn.ActiveLearningClient;
import edu.jhu.hlt.cadet.learn.SortReceiverHandler;
import edu.jhu.hlt.cadet.learn.SortReceiverServer;
import edu.jhu.hlt.concrete.feedback.FeedbackHandler;
import edu.jhu.hlt.concrete.feedback.MemoryFeedbackStore;
import edu.jhu.hlt.concrete.results.MemorySessionStore;
import edu.jhu.hlt.concrete.results.MemoryResultsStore;
import edu.jhu.hlt.concrete.results.ResultsHandler;
import edu.jhu.hlt.concrete.results.ResultsPlugin;
import edu.jhu.hlt.concrete.retriever.RetrieverHandler;
import edu.jhu.hlt.concrete.retriever.RetrieverProvider;
import edu.jhu.hlt.concrete.search.SearchHandler;
import edu.jhu.hlt.concrete.search.SearchProvider;
import edu.jhu.hlt.concrete.send.SenderProvider;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Manages the configuration and dependencies for the CADET search application
 *
 * To access the config object:
 *   ConfigManager.getInstance().getConfig();
 * 
 * The config object should be injected into objects that need it rather than depending on 
 * ConfigManager directly.
 * 
 * The manager constructs and initializes system level dependencies such as the
 * search handler, retrieve handler, and results handler. These handlers are initialized
 * based on configuration information.
 */
public class ConfigManager {

    private static ConfigManager instance;
    private static Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    /**
     * Get the ConfigManager
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private boolean initialized = false;
    private Config config;
    private SearchHandler searchHandler;
    private RetrieverHandler retrieverHandler;
    private ResultsHandler resultsHandler;
    private FeedbackHandler feedbackHandler;
    private SortReceiverServer sortServer;
    private boolean isLearningOn = false;

    private ConfigManager() {}

    /**
     * Initialize the manager
     *
     * Loads the configuration and initializes the dependencies.
     *
     * @param configFile  full path to configuration file
     */
    public void init(String configFile) {
        if (!initialized) {
            loadConfig(configFile);
            createDependencies();
            initialized = true;
        }
    }

    /**
     * Load configuration
     *
     * If no configuration is available, it uses the configuration included in the war/jar.
     *
     * @param configFile  full path to configuration file
     */
    private void loadConfig(String configFile) {
        Config defaultConfig = ConfigFactory.load();

        if (configFile != null && fileExists(configFile)) {
            logger.info("Loading configuration from " + configFile);
            config = ConfigFactory.parseFile(new File(configFile)).withFallback(defaultConfig);
        } else if (configFile != null && !fileExists(configFile)) {
            logger.warn("Cannot access " + configFile);
        } else {
            logger.warn("No configuration file specified");
        }

        if (config == null) {
            logger.warn("Falling back to default configuration");
            config = defaultConfig;
        }
    }

    private void createDependencies() {
        searchHandler = new SearchHandler();
        String spName = config.getString(CadetConfig.SEARCH_PROVIDER);
        SearchProvider sp = (SearchProvider)constructProvider(spName);
        searchHandler.init(sp);

        retrieverHandler = new RetrieverHandler();
        String rpName = config.getString(CadetConfig.RETRIEVE_PROVIDER);
        RetrieverProvider rp = (RetrieverProvider) constructProvider(rpName);
        retrieverHandler.init(rp);

        feedbackHandler = new FeedbackHandler(new MemoryFeedbackStore());

        createResultsServer();
    }

    private void createResultsServer() {
        if (config.hasPath("servlets.learn.status")) {
            if (config.getString("servlets.learn.status").equalsIgnoreCase("on")) {
                isLearningOn = true;
            }
        }
        resultsHandler = new ResultsHandler();
        String sendName = config.getString(CadetConfig.SEND_PROVIDER);
        SenderProvider sender = (SenderProvider) constructProvider(sendName);
        resultsHandler.setSenderProvider(sender);
        String clientName = config.getString(CadetConfig.LEARN_PROVIDER);
        ActiveLearningClient client = (ActiveLearningClient)constructProvider(clientName);
        if (isLearningOn) {
            resultsHandler.setActiveLearningClient(client);
        }
        resultsHandler.setResultsStore(new MemoryResultsStore());
        resultsHandler.setSessionStore(new MemorySessionStore());

        if (config.hasPath("servlets.results.plugins")) {
            List<String> pluginNames = config.getStringList("servlets.results.plugins");
            for (String pluginName : pluginNames) {
                resultsHandler.addPlugin((ResultsPlugin)constructProvider(pluginName));
            }
        }

        if (isLearningOn) {
            SortReceiverHandler handler = new SortReceiverHandler(resultsHandler);
            int port = config.getInt(CadetConfig.SORT_PORT);
            try {
                sortServer = new SortReceiverServer(handler, port);
            } catch (TTransportException e) {
                // probably someone else is using the specified port
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Construct objects according to the pattern of empty constructor and then init(config)
     * @param clazz  full qualified class name
     * @return Provider object
     */
    private Provider constructProvider(String clazz) {
        try {
            Provider provider = (Provider)Class.forName(clazz).getConstructors()[0].newInstance();
            provider.init(config);
            return provider;
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            throw new RuntimeException("Cannot construct " + clazz, ex);
        }
    }

    private static boolean fileExists(String filename) {
        File f = new File(filename);
        return f.exists();
    }

    /**
     * Get the configuration for this application
     */
    public Config getConfig() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return config;
    }

    /**
     * Is active learning on?
     *
     * @return status of active learning
     */
    public boolean isLearningOn() {
        return isLearningOn;
    }

    /**
     * Get the search handler
     */
    public SearchHandler getSearchHandler() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return searchHandler;
    }

    /**
     * Get the retriever handler
     */
    public RetrieverHandler getRetrieverHandler() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return retrieverHandler;
    }

    /**
     * Get the results handler
     */
    public ResultsHandler getResultsHandler() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return resultsHandler;
    }

    /**
     * Get the feedback handler
     */
    public FeedbackHandler getFeedbackHandler() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return feedbackHandler;
    }

    /**
     * Get the sort server
     */
    public SortReceiverServer getSortReceiverServer() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return sortServer;
    }
}
