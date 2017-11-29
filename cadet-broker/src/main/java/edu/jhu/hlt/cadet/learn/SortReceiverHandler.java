/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.learn;

import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.ActiveLearnerClientService;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;
import edu.jhu.hlt.concrete.services.ServiceInfo;

public class SortReceiverHandler implements ActiveLearnerClientService.Iface {
    private static final Logger logger = LoggerFactory.getLogger(SortReceiverHandler.class);

    private SortReceiverCallback callback;

    public SortReceiverHandler(SortReceiverCallback callback) {
        this.callback = callback;
    }

    @Override
    public void submitSort(UUID sessionId, List<AnnotationUnitIdentifier> unitIds) throws TException {
        logger.info("Received sorted list for session " + sessionId.getUuidString());
        callback.addSort(sessionId, unitIds);
    }

    @Override
    public ServiceInfo about() throws TException {
        return new ServiceInfo("Active Learning Sort Receiver", "1.0.0");
    }

    @Override
    public boolean alive() throws TException {
        return true;
    }
}
