package edu.jhu.hlt.cadet.fetch.scion;

import org.apache.thrift.TException;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.retriever.RetrieverProvider;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.scion.ScionConfig;
import edu.jhu.hlt.scion.ScionException;
import edu.jhu.hlt.scion.concrete.server.FetchCommunicationServiceImpl;
import edu.jhu.hlt.scion.core.accumulo.ConnectorFactory;

/**
 * Retrieves communications from scion directly given commIDs
 */
public class ScionRetrieverProvider implements RetrieverProvider {
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
  public boolean alive() throws TException {
    return this.impl.alive();
  }

  @Override
  public ServiceInfo about() throws TException {
    ServiceInfo info = new ServiceInfo("Scion Fetch", "unknown");
    ScionConfig cfg = new ScionConfig();
    info.setDescription("Scion based retriever with config: "
        + "accumulo instance: " + cfg.getAccumuloInstanceName()
        + "zookeeper: " + cfg.getZookeeperServer());
    return info;
  }
}
