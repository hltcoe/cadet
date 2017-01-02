package edu.jhu.hlt.cadet.fetch.scion;

import java.util.List;

import org.apache.thrift.TException;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.fetch.FetchProvider;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.services.NotImplementedException;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.scion.ScionConfig;
import edu.jhu.hlt.scion.ScionException;
import edu.jhu.hlt.scion.concrete.server.FetchCommunicationServiceImpl;
import edu.jhu.hlt.scion.core.accumulo.ConnectorFactory;

/**
 * Fetches communications from scion directly given commIDs
 */
public class ScionFetchProvider implements FetchProvider {
  private FetchCommunicationServiceImpl impl;

  @Override
  public void init(Config config) {
    ScionConfig cfg = new ScionConfig(config);
    ConnectorFactory cf = new ConnectorFactory(cfg);
    try {
      impl = new FetchCommunicationServiceImpl(cf.getConnector());
    } catch (ScionException e) {
      throw new RuntimeException("Error with initializing scion setup.");
    }
  }

  @Override
  public void close() {
  }

  @Override
  public FetchResult fetch(FetchRequest request) throws TException, ServicesException {
    return this.impl.fetch(request);
  }

  @Override
  public long getCommunicationCount() throws NotImplementedException, TException {
    throw new NotImplementedException("scion does not support iteration");
  }

  @Override
  public List<String> getCommunicationIDs(long offset, long count) throws NotImplementedException, TException {
    throw new NotImplementedException("scion does not support iteration");
  }

  @Override
  public boolean alive() throws TException {
    return this.impl.alive();
  }

  @Override
  public ServiceInfo about() throws TException {
    ServiceInfo info = new ServiceInfo("Scion Fetch", "unknown");
    ScionConfig cfg = new ScionConfig();
    info.setDescription("Scion based fetch with config: "
        + "accumulo instance: " + cfg.getAccumuloInstanceName()
        + "zookeeper: " + cfg.getZookeeperServer());
    return info;
  }
}
