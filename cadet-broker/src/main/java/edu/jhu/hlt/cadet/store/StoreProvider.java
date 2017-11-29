/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.store;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.ServiceInfo;

public interface StoreProvider extends Provider {
    /**
     * Store a communication
     *
     * @param communication  a Communication object to store
     * @throws TException
     */
    public void store(Communication communication) throws TException;

    /**
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get information about the store service provider
     */
    public ServiceInfo about() throws TException;
}