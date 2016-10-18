package edu.jhu.hlt.cadet.send;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.access.StoreCommunicationService;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public class SenderHandler implements StoreCommunicationService.Iface {

    private SenderProvider senderProvider;

    /**
     * Initialize the handler - must be called before any other methods
     *
     * @param provider
     */
    public void init(SenderProvider provider) {
        senderProvider = provider;
    }

    @Override
    public void store(Communication communication) throws ServicesException, TException {
        throw new ServicesException("Sender.send() is not currently exposed via a Servlet");
    }

    @Override
    public ServiceInfo about() throws TException {
        return senderProvider.about();
    }

    @Override
    public boolean alive() throws TException {
        return senderProvider.alive();
    }
}
