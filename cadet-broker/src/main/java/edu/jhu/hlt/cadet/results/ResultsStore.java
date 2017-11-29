/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.results;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchResult;
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
    void add(SearchResult results, AnnotationTaskType taskType) throws ServicesException;

    /**
     * Get a specific search result and its information using its ID
     *
     * @param id  identifier of the search result
     * @return results item object or null
     */
    Item getByID(UUID id);

    /**
     * Get the latest search result and its information for a user
     *
     * @param userId  user identifier
     * @return results item object or null
     */
    Item getLatest(String userId);

    /**
     * Get a list of search results filtered by annotation task ordered by latest to oldest
     *
     * @param taskType  annotation task
     * @param limit  maximum number of search results to return (0 for no limit)
     * @return list of search results (empty if no matching search results)
     */
    List<SearchResult> getByTask(AnnotationTaskType taskType, int limit);

    /**
     * Get a list of search results filtered by annotation task and user ordered by latest to oldest
     *
     * @param taskType  annotation task
     * @param userId  user identifier
     * @param limit  maximum number of search results (0 for no limit)
     * @return list of search results (empty if no matching search results)
     */
    List<SearchResult> getByUser(AnnotationTaskType taskType, String userId, int limit);

    public static class Item implements Comparable<Item> {
        public SearchResult results;
        public Set<AnnotationTaskType> tasks;
        public Instant timestamp;
        public String userId;

        public Item(SearchResult results, AnnotationTaskType taskType) {
            this.results = results;
            this.tasks = new HashSet<AnnotationTaskType>();
            this.tasks.add(taskType);
            this.timestamp = Instant.now();
            this.userId = results.getSearchQuery().getUserId();
        }

        public void addTask(AnnotationTaskType taskType) {
            tasks.add(taskType);
        }

        @Override
        public int compareTo(Item otherItem) {
            // newest first
            return otherItem.timestamp.compareTo(timestamp);
        }
    }
}
