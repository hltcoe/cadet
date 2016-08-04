package edu.jhu.hlt.concrete.feedback.store;

import java.util.Map;
import java.util.Set;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.util.ConcreteException;

/**
 * Store search relevance feedback
 */
public interface FeedbackStore extends Provider {
    /**
     * Enroll a search result for feedback
     *
     * @param results  The search result object
     */
    public void addSearchResults(SearchResults results) throws ConcreteException;

    /**
     * Add feedback on a search result item
     * 
     * The search result must be added first.
     *
     * @param uuid  The ID of the search result
     * @param communicationId  The ID of the communication in the search result item
     * @param feedback  The feedback to apply to the communication
     */
    public void addFeedback(UUID uuid, String communicationId, SearchFeedback feedback) throws FeedbackException;

    /**
     * Add feedback on a search result item
     * 
     * The search result must be added first.
     *
     * @param uuid  The ID of the search result
     * @param communicationId  The ID of the communication in the search result item
     * @param sentenceId  The ID of the sentence in the search results item
     * @param feedback  The feedback to apply to the communication
     */
    public void addFeedback(UUID uuid, String communicationId, UUID sentenceId, SearchFeedback feedback) throws FeedbackException;

    /**
     * Get the feedback for a search result of communications
     * 
     * @param uuid  The ID of the search results
     * @return map of communication ID to feedback value or null if no search results
     */
    public Map<String, SearchFeedback> getCommunicationFeedback(UUID uuid);

    /**
     * Get the feedback for a search result of sentences
     * 
     * @param uuid  The ID of the search results
     * @return map of sentence identifiers to feedback value or null if no search results
     */
    public Map<SentenceIdentifier, SearchFeedback> getSentenceFeedback(UUID uuid);

    /**
     * Query for communication feedback
     * 
     * @param query  a query object
     * @return set of communication feedback results
     */
    public Set<CommunicationFeedback> queryCommunicationFeedback(FeedbackQuery query);

    /**
     * Query for Sentence feedback
     * 
     * @param query  a query object
     * @return set of sentence feedback results
     */
    public Set<SentenceFeedback> querySentenceFeedback(FeedbackQuery query);

    /**
     * Get all feedback on communication search results
     * 
     * @return set of communication feedback results
     */
    public Set<CommunicationFeedback> getAllCommunicationFeedback();

    /**
     * Get all feedback on sentences search results
     * 
     * @return set of sentence feedback results
     */
    public Set<SentenceFeedback> getAllSentenceFeedback();
}
