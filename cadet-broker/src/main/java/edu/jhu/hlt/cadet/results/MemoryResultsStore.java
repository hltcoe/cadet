/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.ServicesException;

public class MemoryResultsStore implements ResultsStore {

    private Map<UUID, Item> data = new HashMap<UUID, Item>();
    private Object dataLock = new Object();

    @Override
    public void add(SearchResult results, AnnotationTaskType taskType) throws ServicesException {
        synchronized(dataLock) {
            if (data.containsKey(results.getUuid())) {
                // unexpected behavior will be caused if apps reuse UUIDs for search results
                data.get(results.getUuid()).addTask(taskType);
            } else {
                data.put(results.getUuid(), new Item(results, taskType));
            }
        }
    }

    @Override
    public Item getByID(UUID id) {
        synchronized(dataLock) {
            Item item = data.get(id);
            if (item != null) {
                return item;
            } else {
                return null;
            }
        }
    }

    @Override
    public Item getLatest(String userId) {
        Entry<UUID, Item> item = null;
        synchronized(dataLock) {
            item = data.entrySet().stream()
                        .filter(entry -> userId.equals(entry.getValue().userId))
                        .sorted(Map.Entry.comparingByValue())
                        .findFirst().orElse(null);
        }
        if (item != null) {
            return item.getValue();
        }

        return null;
    }

    @Override
    public List<SearchResult> getByTask(AnnotationTaskType taskType, int limit) {
        if (limit == 0) {
            limit = Integer.MAX_VALUE;
        }
        List<SearchResult> results = null;
        synchronized(dataLock) {
            results = data.entrySet().stream()
                            .filter(entry -> entry.getValue().tasks.contains(taskType))
                            .sorted(Map.Entry.comparingByValue())
                            .limit(limit)
                            .map(entry -> entry.getValue().results)
                            .collect(Collectors.toList());
        }
        return results;
    }

    @Override
    public List<SearchResult> getByUser(AnnotationTaskType taskType, String userId, int limit) {
        if (limit == 0) {
            limit = Integer.MAX_VALUE;
        }
        List<SearchResult> results = null;
        synchronized(dataLock) {
            results = data.entrySet().stream()
                            .filter(entry -> userId.equals(entry.getValue().userId))
                            .filter(entry -> entry.getValue().tasks.contains(taskType))
                            .sorted(Map.Entry.comparingByValue())
                            .limit(limit)
                            .map(entry -> entry.getValue().results)
                            .collect(Collectors.toList());
        }
        return results;
    }

}
