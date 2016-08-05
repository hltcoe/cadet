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
    public static final String SEARCH_HOST = "cadet.search.host";
    public static final String SEARCH_PORT = "cadet.search.port";
    public static final String SEARCH_PROVIDER = "cadet.search.provider";

    /**
     * Retrieve
     */
    public static final String RETRIEVE_HOST = "cadet.retrieve.host";
    public static final String RETRIEVE_PORT = "cadet.retrieve.port";
    public static final String RETRIEVE_PROVIDER = "cadet.retrieve.provider";

    /**
     * Send
     */
    public static final String SEND_HOST = "cadet.send.host";
    public static final String SEND_PORT = "cadet.send.port";
    public static final String SEND_PROVIDER = "cadet.send.provider";

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

    /**
     * File based system (rather than accumulo based)
     */
    public static final String FILES_DATA_DIR = "cadet.files.data.dir";
}
