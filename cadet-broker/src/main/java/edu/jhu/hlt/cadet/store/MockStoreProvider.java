/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.store;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.ServiceInfo;

/**
 * Receives communications for testing, debugging, and development
 */
public class MockStoreProvider implements StoreProvider {
    private static Logger logger = LoggerFactory.getLogger(MockStoreProvider.class);

    @Override
    public void init(Config config) {}

    @Override
    public void close() {}

    @Override
    public void store(Communication communication) throws TException {
        logger.info("Storing Comm Id: " + communication.getId());
    }

    @Override
    public boolean alive() throws TException {
        return true;
    }

    @Override
    public ServiceInfo about() throws TException {
        return new ServiceInfo(this.getClass().getSimpleName(), "1.0.0");
    }

}
