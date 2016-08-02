package edu.jhu.hlt.concrete.send;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.ServiceInfo;

/**
 * Receives communications for testing, debugging, and development
 */
public class MockSenderProvider implements SenderProvider {
    private static Logger logger = LoggerFactory.getLogger(MockSenderProvider.class);

    @Override
    public void init(Config config) {}

    @Override
    public void send(Communication communication) throws TException {
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
