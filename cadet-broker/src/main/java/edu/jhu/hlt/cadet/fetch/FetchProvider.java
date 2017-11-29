/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.fetch;

import java.util.List;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.services.NotImplementedException;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public interface FetchProvider extends Provider {
    /**
     * Fetch communications
     *
     * @param request  a request object with IDs to fetch
     * @return fetch results with a list of communications
     */
    public FetchResult fetch(FetchRequest request) throws ServicesException, TException;

    /**
     * Get a count of communications currently stored in this fetch provider
     *
     * @return count
     */
    public long getCommunicationCount() throws NotImplementedException, TException;

    /**
     * Get a set of communication IDs to support iteration over all communications
     *
     * @param offset zero-based offset
     * @param count the maximum number of communications to return
     * @return list of communication IDs to be used in a fetch request
     */
    public List<String> getCommunicationIDs(long offset, long count) throws NotImplementedException, TException;

    /**
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get information about the fetch provider
     */
    public ServiceInfo about() throws TException;
}
