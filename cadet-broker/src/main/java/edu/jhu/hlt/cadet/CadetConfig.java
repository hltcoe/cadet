package edu.jhu.hlt.cadet;

public class CadetConfig {
    /**
     * Sort Receiver for active learning
     */
    public static final String SORT_HOST = "cadet.sort.host";
    public static final String SORT_PORT = "cadet.sort.port";
    // how often should the mock push new sorts in seconds
    public static final String SORT_PERIOD = "cadet.sort.period";

    /**
     * Active Learning
     */
    // is active learning on? "on" or "off"
    public static final String LEARN_STATUS = "cadet.learn.status";
    public static final String LEARN_HOST = "cadet.learn.host";
    public static final String LEARN_PORT = "cadet.learn.port";
    public static final String LEARN_PROVIDER = "cadet.learn.provider";

    /**
     * Search
     */
    public static final String SEARCH_PROVIDERS = "cadet.search.providers";
    // Custom search settings are relative to the provider name path
    // (e.g. the search provider named "cadet.search.providers.foo" has
    // the path "cadet.search.providers.foo.host", etc).
    public static final String SEARCH_HOST_CUSTOM = "host";
    public static final String SEARCH_PORT_CUSTOM = "port";
    public static final String SEARCH_PROVIDER_CUSTOM = "provider";

    /**
     * Fetch
     */
    public static final String FETCH_HOST = "cadet.fetch.host";
    public static final String FETCH_PORT = "cadet.fetch.port";
    public static final String FETCH_PROVIDER = "cadet.fetch.provider";
    public static final String FETCH_PATHNAME = "cadet.fetch";
    public static final String DEPRECATED_FETCH_PATHNAME = "cadet.retrieve";

    /**
     * Store
     */
    public static final String STORE_HOST = "cadet.store.host";
    public static final String STORE_PORT = "cadet.store.port";
    public static final String STORE_PROVIDER = "cadet.store.provider";
    public static final String STORE_PATHNAME = "cadet.store";
    public static final String DEPRECATED_STORE_PATHNAME = "cadet.send";

    /**
     * Feedback
     */
    public static final String FEEDBACK_DIR = "cadet.feedback.dump_dir";
    public static final String FEEDBACK_STORE = "cadet.feedback.store";

    /**
     * Results server
     */
    public static final String RESULTS_STORE = "cadet.results.store";
    public static final String RESULTS_SESSION = "cadet.results.session";
    public static final String RESULTS_PLUGINS = "cadet.results.plugins";
    public static final String RESULTS_BASE = "cadet.results";
    public static final String RESULTS_CHUNK_SIZE = "chunk_size";
    public static final String RESULTS_ANNOTATION_DEADLINE = "deadline";

    /**
     * File based system (rather than accumulo based)
     */
    public static final String FILES_DATA_DIR = "cadet.files.data.dir";
}
