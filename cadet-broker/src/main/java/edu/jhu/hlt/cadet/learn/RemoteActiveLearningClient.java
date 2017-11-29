/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.learn;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.ActiveLearnerServerService;
import edu.jhu.hlt.concrete.learn.Annotation;
import edu.jhu.hlt.concrete.learn.AnnotationTask;
import edu.jhu.hlt.concrete.services.AsyncContactInfo;

public class RemoteActiveLearningClient implements ActiveLearningClient {
    private static final Logger logger = LoggerFactory.getLogger(RemoteActiveLearningClient.class);

    private AsyncContactInfo sortReceiverInfo = new AsyncContactInfo();
    private String learnerHost;
    private int learnerPort;

    private TFramedTransport transport;
    private TCompactProtocol protocol;
    private ActiveLearnerServerService.Client client;

    @Override
    public void init(Config config) {
        // config overrides the auto detected host name
        if (config.hasPath(CadetConfig.SORT_HOST)) {
            sortReceiverInfo.setHost(config.getString(CadetConfig.SORT_HOST));
        } else {
            try {
                sortReceiverInfo.setHost(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                throw new RuntimeException("Cannot get hostname for sort server. Please specify "
                                + CadetConfig.SORT_HOST + " in configuration.");
            }
        }
        sortReceiverInfo.setPort(config.getInt(CadetConfig.SORT_PORT));
        learnerHost = config.getString(CadetConfig.LEARN_HOST);
        learnerPort = config.getInt(CadetConfig.LEARN_PORT);

        transport = new TFramedTransport(new TSocket(learnerHost, learnerPort), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new ActiveLearnerServerService.Client(protocol);
    }

    @Override
    public boolean start(UUID sessionId, AnnotationTask task) {
        boolean success = false;
        try {
            transport.open();
            success = client.start(sessionId, task, sortReceiverInfo);
        } catch (TException e) {
            logger.warn("Unable to connect to the remote learner", e);
        } finally {
            transport.close();
        }
        return success;
    }

    @Override
    public void stop(UUID sessionId) {
        try {
            transport.open();
            client.stop(sessionId);
        } catch (TException e) {
            logger.warn("Unable to connect to the remote learner", e);
        } finally {
            transport.close();
        }
    }

    @Override
    public void addAnnotations(UUID sessionId, List<Annotation> annotations) {
        try {
            transport.open();
            client.addAnnotations(sessionId, annotations);
        } catch (TException e) {
            logger.warn("Unable to connect to the remote learner", e);
        } finally {
            transport.close();
        }
    }

    @Override
    public void close() {
        if (transport.isOpen()) {
            transport.close();
        }
    }

}
