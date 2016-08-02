package edu.jhu.hlt.concrete.results;

import java.util.List;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.ServicesException;

public interface ResultsStore {
    /**
     * Add a search result and annotation task to the store
     *
     * If the search result has already been added, just add the annotation task
     * 
     * @param results  search results object
     * @param taskType  annotation task
     */
    void add(SearchResults results, AnnotationTaskType taskType) throws ServicesException;

    /**
     * Get a specific search result using its ID
     *
     * @param id  identifier of the search result
     * @return search object or null
     */
    SearchResults getByID(UUID id);

    /**
     * Get the latest search result for a user
     *
     * @param userId  user identifier
     * @return search result or null
     */
    SearchResults getLatest(String userId);

    /**
     * Get a list of search results filtered by annotation task ordered by latest to oldest
     *
     * @param taskType  annotation task
     * @param limit  maximum number of search results to return (0 for no limit)
     * @return list of search results (empty if no matching search results)
     */
    List<SearchResults> getByTask(AnnotationTaskType taskType, int limit);

    /**
     * Get a list of search results filtered by annotation task and user ordered by latest to oldest
     *
     * @param taskType  annotation task
     * @param userId  user identifier
     * @param limit  maximum number of search results (0 for no limit)
     * @return list of search results (empty if no matching search results)
     */
    List<SearchResults> getByUser(AnnotationTaskType taskType, String userId, int limit);
}
