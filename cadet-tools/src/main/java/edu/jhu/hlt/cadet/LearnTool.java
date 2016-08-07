package edu.jhu.hlt.cadet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhu.hlt.cadet.learn.SortReceiverCallback;
import edu.jhu.hlt.cadet.learn.SortReceiverHandler;
import edu.jhu.hlt.cadet.learn.SortReceiverServer;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.learn.ActiveLearnerServer;
import edu.jhu.hlt.concrete.learn.AnnotationTask;
import edu.jhu.hlt.concrete.services.AnnotationTaskType;
import edu.jhu.hlt.concrete.services.AnnotationUnitIdentifier;
import edu.jhu.hlt.concrete.services.AnnotationUnitType;
import edu.jhu.hlt.concrete.services.AsyncContactInfo;

public class LearnTool implements AutoCloseable {
    private TTransport transport;
    private TCompactProtocol protocol;
    private ActiveLearnerServer.Client client;
    private UUID sessionId;
    private SortReceiverServer server;
    private Thread serverThread;
    private int serverPort = 9876;
    private String serverHost = "localhost";

    public LearnTool() throws TTransportException {
        Config config = ConfigFactory.load();
        String host = config.getString(CadetConfig.LEARN_HOST);
        int port = config.getInt(CadetConfig.LEARN_PORT);
        if (config.hasPath(CadetConfig.SORT_PORT)) {
            serverPort = config.getInt(CadetConfig.SORT_PORT);
        }

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new ActiveLearnerServer.Client(protocol);

        try {
            serverHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.err.println("Failed to get host name of this machine. Using localhost.");
        }

        sessionId = new UUID(java.util.UUID.randomUUID().toString());
        startServer();
    }

    public void start(String filename, String lang) throws TException {
        if (!new File(filename).exists()) {
            throw new RuntimeException("File " + filename + " does not exist");
        }

        AnnotationTask task = getAnnotationTask(filename, lang);

        AsyncContactInfo info = new AsyncContactInfo();
        info.setHost(serverHost);
        info.setPort(serverPort);

        transport.open();
        client.start(sessionId, task, info);
        transport.close();
    }

    private AnnotationTask getAnnotationTask(String filename, String lang) {
        AnnotationTask task = new AnnotationTask();
        task.setLanguage(lang);
        task.setType(AnnotationTaskType.NER);
        task.setUnitType(AnnotationUnitType.COMMUNICATION);
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                AnnotationUnitIdentifier unitId = new AnnotationUnitIdentifier();
                unitId.setCommunicationId(line);
                task.addToUnits(unitId);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return task;
    }

    public void stop() throws TException {
        transport.open();
        client.stop(sessionId);
        transport.close();
    }

    private void startServer() throws TTransportException {
        SortReceiverHandler handler = new SortReceiverHandler(new EmptyCallback());
        server = new SortReceiverServer(handler, serverPort);
        serverThread = new Thread(server);
        serverThread.start();        
    }

    private void stopServer() {
        if (serverThread != null) {
            server.close();
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                // whatever
            }
        }
    }

    @Override
    public void close() {
        if (transport.isOpen()) {
            transport.close();
        }
        stopServer();
    }

    private class EmptyCallback implements SortReceiverCallback {
        @Override
        public void addSort(UUID sessionId, List<AnnotationUnitIdentifier> unitIds) {
            System.out.println("Received new sort order");
        }
    }

    static class Opts {
        @Parameter(description = "tsv filename", required = true)
        List<String> filename;

        @Parameter(names = {"--lang", "-l"}, description = "Language code ISO 639-2/T")
        String lang = "zho";

        @Parameter(names = {"--time", "-t"}, description = "Time in minutes to run active learning")
        int time = 2;

        @Parameter(help = true, names = {"--help", "-h"}, description = "Print the help message and exit.")
        boolean help;
    }

    public static void main(String[] args) throws InterruptedException, TException {
        Opts opts = new Opts();
        JCommander jc = new JCommander(opts);
        jc.setProgramName("./learn.sh");
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

        try (LearnTool tool = new LearnTool();) {
            tool.start(opts.filename.get(0), opts.lang);
            Thread.sleep(opts.time * 60 * 1000);
            tool.stop();
        }
    }

}
