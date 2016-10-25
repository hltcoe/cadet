package edu.jhu.hlt.cadet.fetch;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
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
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get information about the fetch provider
     */
    public ServiceInfo about() throws TException;
}
