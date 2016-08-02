package edu.jhu.hlt.concrete.retriever;

import java.io.File;
import java.nio.file.Paths;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.access.RetrieveRequest;
import edu.jhu.hlt.concrete.access.RetrieveResults;
import edu.jhu.hlt.concrete.serialization.CompactCommunicationSerializer;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.util.ConcreteException;

/**
 * Assumes communications are stored as <comm id>.concrete
 * The concrete communications must be serialized with compact protocol.
 * Requires that the config parameter be set: files.data.dir
 */
public class FileRetrieverProvider implements RetrieverProvider {
    private static Logger logger = LoggerFactory.getLogger(FileRetrieverProvider.class);

    public static final String EXTENSION = "concrete";

    private String directory;
    private final CompactCommunicationSerializer serializer;

    public FileRetrieverProvider() {
        serializer = new CompactCommunicationSerializer();
    }

    @Override
    public void init(Config config) {
        if (!config.hasPath("files.data.dir")) {
            throw new RuntimeException("Directory is not set in config: files.data.dir");
        }
        directory = config.getString("files.data.dir");
        if (directory.charAt(directory.length() - 1) != File.separatorChar) {
            directory += File.separator;
        }

        File file = new File(directory);
        if (!file.exists()) {
            throw new RuntimeException("Directory " + directory + " does not exist");
        }
        if (!file.isDirectory()) {
            throw new RuntimeException(directory + " is not a directory");
        }
    }

    @Override
    public RetrieveResults retrieve(RetrieveRequest request) throws ServicesException, TException {
        RetrieveResults results = new RetrieveResults();

        for (String id : request.getCommunicationIds()) {
            String path = directory + id + "." + EXTENSION;
            try {
                Communication comm = serializer.fromPath(Paths.get(path));
                results.addToCommunications(comm);
            } catch (ConcreteException e) {
                logger.warn("Unable to read " + path, e);
            }
        }

        return results;
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
