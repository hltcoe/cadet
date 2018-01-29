/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.results;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;

/**
 * Passive expiring set
 *
 * Call expire() to remove elements that have been in the set too long.
 */
public class ExpiringSet implements Set<AnnotationUnitIdentifier> {

    private final Set<AnnotationUnitIdentifier> data = new HashSet<>();
    private final Map<AnnotationUnitIdentifier, Long> timestamps = new HashMap<>();
    private final long timeToLive;

    /**
     * Create a set that can passively expire items
     *
     * @param timeToLive  time the item remains in set in milliseconds
     */
    public ExpiringSet(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Set<AnnotationUnitIdentifier> expire() {
        long currentTime = getTime();
        Set<AnnotationUnitIdentifier> expired = new HashSet<>();
        for (Iterator<Map.Entry<AnnotationUnitIdentifier, Long>> it =
                        timestamps.entrySet().iterator(); it.hasNext();) {
            Map.Entry<AnnotationUnitIdentifier, Long> entry = it.next();
            if ((currentTime - entry.getValue()) > timeToLive) {
                expired.add(entry.getKey());
                data.remove(entry.getKey());
                it.remove();
            }
        }
        return expired;
    }

    @Override
    public boolean add(AnnotationUnitIdentifier item) {
        boolean isNewItem = data.add(item);
        if (isNewItem) {
            timestamps.put(item, getTime());
        }
        return isNewItem;
    }

    @Override
    public boolean addAll(Collection<? extends AnnotationUnitIdentifier> c) {
        boolean isModified = false;
        for (AnnotationUnitIdentifier o : c) {
            isModified |= add(o);
        }
        return isModified;
    }

    @Override
    public void clear() {
        data.clear();
        timestamps.clear();
    }

    @Override
    public boolean contains(Object o) {
        return data.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return data.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public Iterator<AnnotationUnitIdentifier> iterator() {
        return data.iterator();
    }

    @Override
    public boolean remove(Object o) {
        boolean isInSet = data.remove(o);
        if (isInSet) {
            timestamps.remove(o);
        }
        return isInSet;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean isModified = false;
        for (Object o : c) {
            isModified |= remove(o);
        }
        return isModified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean isModified = data.retainAll(c);
        for (AnnotationUnitIdentifier item : timestamps.keySet()) {
            if (!data.contains(item)) {
                timestamps.remove(item);
            }
        }
        return isModified;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Object[] toArray() {
        return data.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return data.toArray(a);
    }

    protected long getTime() {
        return System.currentTimeMillis();
    }

}
