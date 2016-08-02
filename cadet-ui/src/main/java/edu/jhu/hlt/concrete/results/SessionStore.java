package edu.jhu.hlt.concrete.results;

import java.util.List;

import edu.jhu.hlt.concrete.UUID;

public interface SessionStore {
    /**
     * Add a session to the store
     *
     * @param session  session object
     */
    public void add(AnnotationSession session);

    /**
     * Remove a session from the store
     *
     * @param session  session object
     */
    public void remove(AnnotationSession session);

    /**
     * Remove a session from the store
     *
     * @param id  session id
     */
    public void remove(UUID id);

    /**
     * Get a session based on its ID
     *
     * @param id  session id
     * @return session object or null
     */
    public AnnotationSession get(UUID id);

    /**
     * List active sessions
     *
     * @return list of session objects
     */
    public List<AnnotationSession> list();
}
