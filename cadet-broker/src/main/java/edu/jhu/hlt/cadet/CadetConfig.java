package edu.jhu.hlt.cadet;

public class CadetConfig {
    /**
     * Sort Receiver for active learning
     */
    public static final String SORT_HOST = "servlets.sort.host";
    public static final String SORT_PORT = "servlets.sort.port";
    // how often should the mock push new sorts in seconds
    public static final String SORT_PERIOD = "servlets.sort.period";

    /**
     * Active Learning
     */
    public static final String LEARN_HOST = "servlets.learn.host";
    public static final String LEARN_PORT = "servlets.learn.port";
    public static final String LEARN_PROVIDER = "servlets.learn.provider";

    /**
     * Search
     */
    public static final String SEARCH_HOST = "servlets.search.host";
    public static final String SEARCH_PORT = "servlets.search.port";
    public static final String SEARCH_PROVIDER = "servlets.search.provider";

    /**
     * Retrieve
     */
    public static final String RETRIEVE_HOST = "servlets.retrieve.host";
    public static final String RETRIEVE_PORT = "servlets.retrieve.port";
    public static final String RETRIEVE_PROVIDER = "servlets.retrieve.provider";

    /**
     * Send
     */
    public static final String SEND_HOST = "servlets.send.host";
    public static final String SEND_PORT = "servlets.send.port";
    public static final String SEND_PROVIDER = "servlets.send.provider";

    /**
     * Feedback
     */
    public static final String FEEDBACK_DIR = "servlets.feedback.dump_dir";
    public static final String FEEDBACK_STORE = "servlets.feedback.store";

    /**
     * Results server
     */
    public static final String RESULTS_STORE = "servlets.results.store";
    public static final String RESULTS_SESSION = "servlets.results.session";
}
