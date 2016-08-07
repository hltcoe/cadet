package edu.jhu.hlt.cadet.retriever;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import org.apache.thrift.TException;

import edu.jhu.hlt.concrete.access.RetrieveRequest;
import edu.jhu.hlt.concrete.access.RetrieveResults;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.services.ServiceInfo;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.scion.ScionConfig;
import edu.jhu.hlt.scion.ScionException;
import edu.jhu.hlt.scion.concrete.AccumuloCommunicationIterator;
import edu.jhu.hlt.scion.concrete.AnalyticGroup;
import edu.jhu.hlt.scion.concrete.ConcreteConnector;
import edu.jhu.hlt.scion.concrete.analytics.Analytics;
import edu.jhu.hlt.scion.core.accumulo.TableOps;
import edu.jhu.hlt.scion.core.accumulo.ScionConnector;
import edu.jhu.hlt.scion.core.accumulo.ConnectorFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * Retrieves communications from scion directly given commIDs
 */
public class ScionRetrieverProvider implements RetrieverProvider {
    private static Logger logger = LoggerFactory.getLogger(ScionRetrieverProvider.class);

    private AnalyticGroup ag;
    private ConcreteConnector cc;
    private ConnectorFactory cf;

    private String zookeeper;
    private String accumuloInstance;

    public void init(Config config) {
        try {
            cf = new ConnectorFactory();
            ScionConnector sc = cf.getConnector();
            ag = AnalyticGroup.create(sc, new Analytics(sc).createAllAvailableAnalytics());
            cc = new ConcreteConnector(sc);

            ScionConfig sconfig = new ScionConfig(config);
            zookeeper = sconfig.getZookeeperServer();
            accumuloInstance = sconfig.getAccumuloInstanceName();
        } catch (ScionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {}

    @Override
    public RetrieveResults retrieve(RetrieveRequest request) throws TException, ServicesException {

        if (request.getCommunicationIdsSize() == 0) {
            logger.warn("Ignoring request to retrieve zero communications");
            return new RetrieveResults(new ArrayList<Communication>());
        }

        List<String> commIdList = request.getCommunicationIds();
        try (AccumuloCommunicationIterator commIter = (AccumuloCommunicationIterator)cc.byIds(commIdList, ag)) {
            List<Communication> commList = new ArrayList<>();
            while (commIter.hasNext()) {
                Communication comm = commIter.next();
                commList.add(comm);
            }
            return new RetrieveResults(commList);
        } catch (ScionException e) {
            // Errors from Accumulo on loading individual communications get turned into Runtime exceptions.
            // This catch is for failing to connect to Accumulo.
            logger.warn("Failed to retrieve results due to " + e.getMessage(), e);
            throw new ServicesException("Unable to connect to Accumulo");
        }
    }

    @Override
    public boolean alive() throws TException {
        try {
            new TableOps(cf.getConnector()).tableExists("documents");
            return true;
        } catch (ScionException e) {
            return false;
        }
    }

    @Override
    public ServiceInfo about() throws TException {
        ServiceInfo info = new ServiceInfo("Scion Retrieve", "1.0.0");
        info.setDescription("Scion based retriever with config: "
                        + "accumulo instance: " + accumuloInstance
                        + "zookeeper: " + zookeeper
        );
        return info;
    }
}
