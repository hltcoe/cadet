package edu.jhu.hlt.cadet.retriever;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.access.RetrieveRequest;
import edu.jhu.hlt.concrete.access.RetrieveResults;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public interface RetrieverProvider extends Provider {
    /**
     * Retrieve communications
     * 
     * @param request  a request object with IDs to retrieve
     * @return retrieve results with a list of communications
     */
    public RetrieveResults retrieve(RetrieveRequest request) throws ServicesException, TException;

    /**
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get information about the search provider
     */
    public ServiceInfo about() throws TException;
}
