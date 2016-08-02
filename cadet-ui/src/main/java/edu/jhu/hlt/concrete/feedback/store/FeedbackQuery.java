package edu.jhu.hlt.concrete.feedback.store;

import java.util.Date;

/**
 * Query object for feedback
 * <p>
 * Provides methods for setting filters on the feedback. The filters supported are
 * <ul>
 * <li>Limit on the number of reports returned
 * <li>Query names
 * <li>User names
 * <li>Labels
 * <li>Date range
 * </ul>
 */
public class FeedbackQuery {
    private int limit;
    private String[] userNames;
    private String[] queryNames;
    private String[] labels;
    private Date startDate;
    private Date endDate;

    public static final int NO_LIMIT = -1;
    private static final int DEFAULT_LIMIT = -1;

    public FeedbackQuery() {
        limit = DEFAULT_LIMIT;
    }

    /**
     * Get the maximum number of reports to return
     *
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Set the maximum number of reports to return
     *
     * @param limit  The maximum number of reports to return
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Turn off the limit
     */
    public void setLimitOff() {
        limit = NO_LIMIT;
    }

    /**
     * Get the names of the queries being filtered for
     *
     * @return the query names being filtered for
     */
    public String[] getQueryNames() {
        return queryNames;
    }

    /**
     * Set the names of the queries to filter for
     *
     * @param names  The queries to filter for
     */
    public void setQueryNames(String[] names) {
        queryNames = names;
    }

    /**
     * Set the name of the query to filter for
     *
     * @param name  The query to filter for
     */
    public void setQueryName(String name) {
        this.queryNames = new String[1];
        this.queryNames[0] = name;
    }

    /**
     * Get the names of the users being filtered for
     *
     * @return the user names being filtered for
     */
    public String[] getUserNames() {
        return userNames;
    }

    /**
     * Set the names of the users to filter for
     *
     * @param names  The users to filter for
     */
    public void setUserNames(String[] names) {
        userNames = new String[names.length];
        System.arraycopy(names, 0, this.userNames, 0, names.length );
    }

    /**
     * Set the name of the user to filter for
     *
     * @param name  The user to filter for
     */
    public void setUserName(String name) {
        this.userNames = new String[1];
        this.userNames[0] = name;
    }

    /**
     * Get the labels being filtered for
     *
     * @return the labels being filtered for
     */
    public String[] getLabels() {
        return labels;
    }

    /**
     * Set the labels to filter for
     *
     * @param labels  The labels to filter for
     */
    public void setLabels(String[] labels) {
        this.labels = new String[labels.length];
        System.arraycopy(labels, 0, this.labels, 0, labels.length );
    }

    /**
     * Get the start of the date range
     *
     * @return the start of the date range filter
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set the start of the date range
     *
     * @param startDate  the beginning of the date range
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get the end of the date range
     *
     * @return the end of the date range filter
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set the end of the date range
     *
     * @param endDate  the end of the date range
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Set the start and end of the date range
     *
     * @param startDate  the start of the date range filter
     * @param endDate  the end of the date range filter
     */
    public void setTimeRange(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

}
