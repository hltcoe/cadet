package edu.jhu.hlt.cadet;

import edu.jhu.hlt.cadet.learn.ActiveLearningClient;
import edu.jhu.hlt.cadet.learn.SortReceiverHandler;
import edu.jhu.hlt.cadet.learn.SortReceiverServer;
import edu.jhu.hlt.cadet.feedback.FeedbackHandler;
import edu.jhu.hlt.cadet.feedback.store.FeedbackStore;
import edu.jhu.hlt.cadet.fetch.FetchHandler;
import edu.jhu.hlt.cadet.fetch.FetchProvider;
import edu.jhu.hlt.cadet.results.MemorySessionStore;
import edu.jhu.hlt.cadet.results.MemoryResultsStore;
import edu.jhu.hlt.cadet.results.ResultsHandler;
import edu.jhu.hlt.cadet.results.ResultsPlugin;
import edu.jhu.hlt.cadet.search.SearchProvider;
import edu.jhu.hlt.cadet.search.SearchProxyHandler;
import edu.jhu.hlt.cadet.store.StoreProvider;
import edu.jhu.hlt.cadet.store.StoreHandler;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;

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
 * search handler, fetch handler, and results handler. These handlers are initialized
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
    private Set<Provider> providers = new HashSet<>();
    private SearchProxyHandler searchProxyHandler;
    private FetchHandler fetchHandler;
    private ResultsHandler resultsHandler;
    private FeedbackHandler feedbackHandler;
    private StoreHandler storeHandler;
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
     * Shutdown the manager
     *
     * Frees any resources allocated during initialization
     */
    public void close() {
        logger.info("Shutting down the ConfigManager and freeing its resources");
        for (Provider provider : providers) {
            provider.close();
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

        if (config.hasPath(CadetConfig.DEPRECATED_FETCH_PATHNAME) ||
            config.hasPath(CadetConfig.DEPRECATED_STORE_PATHNAME)) {
            throw new RuntimeException(
                    "Your CADET configuration file is using the deprecated service names '" +
                    CadetConfig.DEPRECATED_FETCH_PATHNAME + "' and/or '" +
                    CadetConfig.DEPRECATED_STORE_PATHNAME + "'. " +
                    "Please update the file to use the new service names '" +
                    CadetConfig.FETCH_PATHNAME + "' and '" +
                    CadetConfig.STORE_PATHNAME + "'");
        }
    }

    private void createDependencies() {
        fetchHandler = new FetchHandler();
        String fpName = config.getString(CadetConfig.FETCH_PROVIDER);
        FetchProvider fp = (FetchProvider)constructProvider(fpName);
        fetchHandler.init(fp);

        String fbStoreName = config.getString(CadetConfig.FEEDBACK_STORE);
        FeedbackStore fbStore = (FeedbackStore)constructProvider(fbStoreName);
        feedbackHandler = new FeedbackHandler(fbStore);

        createResultsServer();
        createSearchProxyHandler();
    }

    private void createResultsServer() {
        if (config.hasPath(CadetConfig.LEARN_STATUS)) {
            if (config.getString(CadetConfig.LEARN_STATUS).equalsIgnoreCase("on")) {
                isLearningOn = true;
            }
        }
        resultsHandler = new ResultsHandler();
        storeHandler = new StoreHandler();
        String spName = config.getString(CadetConfig.STORE_PROVIDER);
        StoreProvider storeProvider = (StoreProvider) constructProvider(spName);
        storeHandler.init(storeProvider);
        resultsHandler.setStoreProvider(storeProvider);
        String clientName = config.getString(CadetConfig.LEARN_PROVIDER);
        ActiveLearningClient client = (ActiveLearningClient)constructProvider(clientName);
        if (isLearningOn) {
            resultsHandler.setActiveLearningClient(client);
        }
        resultsHandler.setResultsStore(new MemoryResultsStore());
        resultsHandler.setSessionStore(new MemorySessionStore());

        if (config.hasPath(CadetConfig.RESULTS_PLUGINS)) {
            List<String> pluginNames = config.getStringList(CadetConfig.RESULTS_PLUGINS);
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

    private void createSearchProxyHandler() {
        if (config.hasPath(CadetConfig.SEARCH_PROVIDERS)) {
            searchProxyHandler = new SearchProxyHandler();

            ConfigObject providersConfig = config.getObject(CadetConfig.SEARCH_PROVIDERS);
            for (String providerName : providersConfig.keySet()) {
                logger.info("providerName: " + providerName);

                // We create a Config object rooted at the name of the SearchProvider
                // (e.g. "cadet.search.providers.foo")
                ConfigObject providerConfigObject = (ConfigObject)providersConfig.get(providerName);
                Config providerConfig = providerConfigObject.toConfig();

                String spName = providerConfig.getString("provider");
                SearchProvider sp = (SearchProvider)constructProvider(spName, providerConfig);
                searchProxyHandler.addProvider(providerName, sp);
            }
        }
    }

    /**
     * Construct objects according to the pattern of empty constructor and then init(config)
     * @param clazz  full qualified class name
     * @return Provider object
     */
    private Provider constructProvider(String clazz) {
        return constructProvider(clazz, config);
    }

    private Provider constructProvider(String clazz, Config customConfig) {
        try {
            Provider provider = (Provider)Class.forName(clazz).getConstructors()[0].newInstance();
            provider.init(customConfig);
            providers.add(provider);
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
     * Get the search proxy handler
     */
    public SearchProxyHandler getSearchProxyHandler() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return searchProxyHandler;
    }

    /**
     * Get the fetch handler
     */
    public FetchHandler getFetchHandler() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return fetchHandler;
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
     * Get the store handler
     */
    public StoreHandler getStoreHandler() {
        if (!initialized) {
            throw new RuntimeException("ConfigManager used before initialized");
        }
        return storeHandler;
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
