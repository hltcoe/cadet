package edu.jhu.hlt.cadet.send;

import org.apache.log4j.Logger;

import java.util.Iterator;

import org.apache.log4j.BasicConfigurator;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.access.Sender;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;

public class SenderHandler implements Sender.Iface {
    private static Logger logger = Logger.getLogger(SenderHandler.class);

    private SenderProvider senderProvider;

    public SenderHandler() {
        BasicConfigurator.configure();
    }

    /**
     * Initialize the handler - must be called before any other methods
     *
     * @param provider
     */
    public void init(SenderProvider provider) {
        senderProvider = provider;
    }

    public void send(Communication communication) throws ServicesException, TException {
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
