/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.learn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.ActiveLearnerClientService;
import edu.jhu.hlt.concrete.learn.Annotation;
import edu.jhu.hlt.concrete.learn.AnnotationTask;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;

/**
 * Creates random sorts
 */
public class FullMockActiveLearningClient implements ActiveLearningClient {
    private static final Logger logger = LoggerFactory.getLogger(FullMockActiveLearningClient.class);

    private Map<UUID, List<AnnotationUnitIdentifier>> data;
    private Object dataLock = new Object();
    private SortCreator sortCreator;
    private Thread sortThread;

    @Override
    public void init(Config config) {
        data = new HashMap<UUID, List<AnnotationUnitIdentifier>>();
        sortCreator = new SortCreator(config, data, dataLock);
        sortThread = new Thread(sortCreator);
        sortThread.start();
    }

    @Override
    public void close() {
        sortCreator.stop();
        try {
            sortThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean start(UUID sessionId, AnnotationTask task) {
        if (data.containsKey(sessionId)) {
            throw new RuntimeException("Active learning session already active " + sessionId.getUuidString());
        }
        logger.info("Starting a learning session " + sessionId.getUuidString());
        synchronized(dataLock) {
            data.put(sessionId, task.getUnits());
        }
        return true;
    }

    @Override
    public void stop(UUID sessionId) {
        if (!data.containsKey(sessionId)) {
            throw new RuntimeException("Active learning session not active " + sessionId.getUuidString());
        }
        logger.info("Stopping learning session " + sessionId.getUuidString());
        synchronized(dataLock) {
            data.remove(sessionId);
        }
    }

    @Override
    public void addAnnotations(UUID sessionId, List<Annotation> annotations) {
        if (!data.containsKey(sessionId)) {
            throw new RuntimeException("Active learning session not active " + sessionId.getUuidString());
        }
        logger.info("Adding " + annotations.size() + " annotations for session " + sessionId.getUuidString());
    }

    private class SortCreator implements Runnable {

        private boolean stopFlag = false;

        private Map<UUID, List<AnnotationUnitIdentifier>> sessions;
        private Object lock;

        private String sortReceiverHost;
        private int sortReceiverPort;

        private TFramedTransport transport;
        private TCompactProtocol protocol;
        private ActiveLearnerClientService.Client client;

        private int periodInSec;

        public SortCreator(Config config, Map<UUID, List<AnnotationUnitIdentifier>> sessions, Object lock) {
            this.sessions = sessions;
            this.lock = lock;
            sortReceiverHost = "localhost";
            sortReceiverPort = config.getInt(CadetConfig.SORT_PORT);

            transport = new TFramedTransport(new TSocket(sortReceiverHost, sortReceiverPort), Integer.MAX_VALUE);
            protocol = new TCompactProtocol(transport);
            client = new ActiveLearnerClientService.Client(protocol);

            periodInSec = 60;
            if (config.hasPath(CadetConfig.SORT_PERIOD)) {
                periodInSec = config.getInt(CadetConfig.SORT_PERIOD);
            }
        }

        @Override
        public void run() {
            while (stopFlag == false) {
                try {
                    Thread.sleep(1000 * periodInSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized(lock) {
                    for (Map.Entry<UUID, List<AnnotationUnitIdentifier>> entry : sessions.entrySet()) {
                        try {
                            java.util.Collections.shuffle(entry.getValue());
                            transport.open();
                            client.submitSort(entry.getKey(), entry.getValue());
                            transport.close();
                        } catch (TException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void stop() {
            stopFlag = true;
        }
    }

}
