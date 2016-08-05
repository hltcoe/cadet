package edu.jhu.hlt.cadet.learn;

import java.util.List;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.Annotation;
import edu.jhu.hlt.concrete.learn.AnnotationTask;

/**
 * Client that talks to an active learner.
 *
 * See CadetConfig for the configuration parameters that AL needs.
 * The parameters are namespaced by LEARN_.
 */
public interface ActiveLearningClient extends Provider {

    /**
     * Start an active learning session
     *
     * @param sessionId  session identifier
     * @param task task  description
     */
    public boolean start(UUID sessionId, AnnotationTask task);

    /**
     * Stop an active learning session
     * 
     * @param sessionId  session identifier
     */
    public void stop(UUID sessionId);

    /**
     * Send annotations to the active learner
     * 
     * @param sessionId  session identifier
     * @param annotations  list of annotations
     */
    public void addAnnotations(UUID sessionId, List<Annotation> annotations);

    /**
     * Shutdown and clean up any resources
     */
    public void close();
}
