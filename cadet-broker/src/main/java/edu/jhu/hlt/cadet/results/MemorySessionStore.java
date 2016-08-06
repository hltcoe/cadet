package edu.jhu.hlt.cadet.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.jhu.hlt.concrete.UUID;

public class MemorySessionStore implements SessionStore {
    private Map<UUID, AnnotationSession> data = new HashMap<UUID, AnnotationSession>();
    private Object dataLock = new Object();

    @Override
    public void add(AnnotationSession session) {
        synchronized(dataLock) {
            data.put(session.getId(), session);
        }
    }

    @Override
    public void remove(AnnotationSession session) {
        remove(session.getId());
    }

    @Override
    public void remove(UUID id) {
        synchronized(dataLock) {
            data.remove(id);
        }
    }

    @Override
    public AnnotationSession get(UUID id) {
        synchronized(dataLock) {
            return data.get(id);
        }
    }

    @Override
    public List<AnnotationSession> list() {
        synchronized(dataLock) {
            return new ArrayList<AnnotationSession>(data.values());
        }
    }

}
