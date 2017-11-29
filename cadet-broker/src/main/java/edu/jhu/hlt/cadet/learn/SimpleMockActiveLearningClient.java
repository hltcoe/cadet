/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.learn;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.Annotation;
import edu.jhu.hlt.concrete.learn.AnnotationTask;

/**
 * Logs the requests for active learning. Never returns new sorts.
 */
public class SimpleMockActiveLearningClient implements ActiveLearningClient {
    private static final Logger logger = LoggerFactory.getLogger(SimpleMockActiveLearningClient.class);

    @Override
    public void init(Config config) {}

    @Override
    public boolean start(UUID sessionId, AnnotationTask task) {
        logger.info("Starting a learning session " + sessionId.getUuidString());
        logger.info("Learning task " + task.getLanguage() + " " + task.getType().toString());
        logger.info("Learning task size " + task.getUnitsSize());
        return true;
    }

    @Override
    public void stop(UUID sessionId) {
        logger.info("Stopping learning session " + sessionId.getUuidString());
    }

    @Override
    public void addAnnotations(UUID sessionId, List<Annotation> annotations) {
        logger.info("Adding " + annotations.size() + " annotations for session " + sessionId.getUuidString());
    }

    @Override
    public void close() {}

}
