package edu.jhu.hlt.cadet.store;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.ServiceInfo;

public interface StoreProvider extends Provider {
    /**
     * Send a communication
     * 
     * @param communication  a Communication object to send
     * @throws TException
     */
    public void send(Communication communication) throws TException;

    /**
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get information about the sender provider
     */
    public ServiceInfo about() throws TException;
}