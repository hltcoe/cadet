package edu.jhu.hlt.cadet;

import java.io.File;
import java.nio.file.Paths;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.access.FetchCommunicationService;
import edu.jhu.hlt.concrete.serialization.CompactCommunicationSerializer;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class FileServer {
    private static Logger logger = LoggerFactory.getLogger(FileServer.class);

    private final int port;
    private final String dataDir;
    private FetchCommunicationService.Processor<FetchCommunicationService.Iface> processor;

    public FileServer(int port, String dataDir) {
        this.port = port;
        this.dataDir = dataDir;
    }

    public void start() {
        processor = new FetchCommunicationService.Processor<FetchCommunicationService.Iface>(new Handler(dataDir));
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    launch(processor);
                } catch (TTransportException e) {
                    logger.error("Failed to start server", e);
                }
            }
        };
        new Thread(task).start();
    }

    public void launch(FetchCommunicationService.Processor<FetchCommunicationService.Iface> processor) throws TTransportException {
        TNonblockingServerTransport transport = new TNonblockingServerSocket(port);
        TNonblockingServer.Args serverArgs = new TNonblockingServer.Args(transport);
        serverArgs = serverArgs.processorFactory(new TProcessorFactory(processor));
        serverArgs = serverArgs.protocolFactory(new TCompactProtocol.Factory());
        serverArgs = serverArgs.transportFactory(new TFramedTransport.Factory(Integer.MAX_VALUE));
        serverArgs.maxReadBufferBytes = Long.MAX_VALUE;
        TNonblockingServer server = new TNonblockingServer(serverArgs);
        logger.info("Starting file retrieve server on port " + port);
        server.serve();
    }

    private class Handler implements FetchCommunicationService.Iface {
        private final String dataDir;
        private final CompactCommunicationSerializer serializer;

        public static final String EXTENSION = "concrete";

        public Handler(String dataDir) {
            this.serializer = new CompactCommunicationSerializer();

            if (dataDir.charAt(dataDir.length() - 1) != File.separatorChar) {
                dataDir += File.separator;
            }
            this.dataDir = dataDir;

            File file = new File(this.dataDir);
            if (!file.exists()) {
                throw new RuntimeException("Directory " + this.dataDir + " does not exist");
            }
            if (!file.isDirectory()) {
                throw new RuntimeException(this.dataDir + " is not a directory");
            }
        }

        @Override
        public FetchResult fetch(FetchRequest request) throws ServicesException, TException {
            FetchResult results = new FetchResult();

            for (String id : request.getCommunicationIds()) {
                String path = dataDir + id + "." + EXTENSION;
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
        public ServiceInfo about() throws TException {
            return new ServiceInfo("File Server", "1.0.0");
        }

        @Override
        public boolean alive() throws TException {
            return true;
        }

    }

    private static class Opts {
        @Parameter(names = {"--port", "-p"},
                description = "The port the server will listen on.")
        int port = 8077;

        @Parameter(names = {"--data", "-d"}, required = true,
                description = "Path to the data directory with the concrete comms.")
        String dataDir;

        @Parameter(names = {"--help", "-h"},
                help = true, description = "Print the usage information and exit.")
        boolean help;
    }

    public static void main(String[] args) {
        Opts opts = new Opts();
        JCommander jc = new JCommander(opts);
        jc.setProgramName("./file_server.sh");
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            jc.usage();
            System.exit(-1);
        }
        if (opts.help) {
            jc.usage();
            return;
        }

        FileServer server = new FileServer(opts.port, opts.dataDir);
        server.start();
    }
}
