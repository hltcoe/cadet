/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.store;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.access.StoreCommunicationService;
import edu.jhu.hlt.cadet.store.StoreProvider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public class StoreHandler implements StoreCommunicationService.Iface {

    private StoreProvider storeProvider;

    /**
     * Initialize the handler - must be called before any other methods
     *
     * @param provider
     */
    public void init(StoreProvider provider) {
        storeProvider = provider;
    }

    @Override
    public void store(Communication communication) throws ServicesException, TException {
        throw new ServicesException("StoreCommunicationService.store() is not currently exposed via a Servlet");
    }

    @Override
    public ServiceInfo about() throws TException {
        return storeProvider.about();
    }

    @Override
    public boolean alive() throws TException {
        return storeProvider.alive();
    }
}
