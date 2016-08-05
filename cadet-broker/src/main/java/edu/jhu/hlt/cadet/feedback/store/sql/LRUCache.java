package edu.jhu.hlt.cadet.feedback.store.sql;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 6880515538385701467L;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private int capacity;

    public LRUCache(int capacity) {
        super(LRUCache.DEFAULT_INITIAL_CAPACITY, LRUCache.DEFAULT_LOAD_FACTOR, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() >= capacity;
    }
}
