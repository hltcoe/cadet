package edu.jhu.hlt.cadet;

public class CadetConfig {
    /**
     * Sort Receiver for active learning
     */
    public static final String SORT_HOST = "sort.host";
    public static final String SORT_PORT = "sort.port";
    // how often should the mock push new sorts in seconds
    public static final String SORT_PERIOD = "sort.period";

    /**
     * Active Learning
     */
    public static final String LEARN_HOST = "learn.host";
    public static final String LEARN_PORT = "learn.port";
    public static final String LEARN_PROVIDER = "learn.provider";

    /**
     * Search
     */
    public static final String SEARCH_HOST = "search.host";
    public static final String SEARCH_PORT = "search.port";
    public static final String SEARCH_PROVIDER = "search.provider";

    /**
     * Retrieve
     */
    public static final String RETRIEVE_HOST = "retrieve.host";
    public static final String RETRIEVE_PORT = "retrieve.port";
    public static final String RETRIEVE_PROVIDER = "retrieve.provider";

    /**
     * Send
     */
    public static final String SEND_HOST = "send.host";
    public static final String SEND_PORT = "send.port";
    public static final String SEND_PROVIDER = "send.provider";

    /**
     * Feedback
     */
    public static final String FEEDBACK_DIR = "feedback.dump_dir";
    public static final String FEEDBACK_STORE = "feedback.store";

    /**
     * Results server
     */
    public static final String RESULTS_STORE = "results.store";
    public static final String RESULTS_SESSION = "results.session";
}
