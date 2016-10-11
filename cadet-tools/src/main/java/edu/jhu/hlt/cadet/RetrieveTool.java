package edu.jhu.hlt.cadet;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhu.hlt.concrete.access.RetrieveRequest;
import edu.jhu.hlt.concrete.access.RetrieveResults;
import edu.jhu.hlt.concrete.access.Retriever;
import edu.jhu.hlt.concrete.serialization.CompactCommunicationSerializer;
import edu.jhu.hlt.concrete.services.ServicesException;

public class RetrieveTool implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveTool.class);

    private String auths;

    private TTransport transport;
    private TCompactProtocol protocol;
    private Retriever.Client client;

    public RetrieveTool() {
        this(ConfigFactory.load());
    }

    public RetrieveTool(Config cfg) {
        this(cfg.getString(CadetConfig.RETRIEVE_HOST),
            cfg.getInt(CadetConfig.RETRIEVE_PORT),
            cfg.getString("cadet.accumulo.auths"));
    }

    public RetrieveTool(String host, int port, String auths) {
        LOGGER.debug("Running with host: {}, port: {}, auths: {}", host, port, auths);
        this.auths = auths;
        this.transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        this.protocol = new TCompactProtocol(transport);
        this.client = new Retriever.Client(protocol);
    }

    public RetrieveResults retrieve(Iterable<String> ids) throws ServicesException, TException {
        if (!this.transport.isOpen()) {
            this.transport.open();
        }
        RetrieveRequest rr = new RetrieveRequest().setAuths(this.auths)
                        .setCommunicationIds(new ArrayList<>());
        ids.forEach(rr::addToCommunicationIds);
        return this.client.fetch(rr);
    }

    @Override
    public void close() {
        if (this.transport.isOpen()) {
            this.transport.close();
        }
    }

    private static void toStdout(RetrieveResults rr) {
        System.out.println("Comm ID\tComm UUID\tText");
        rr.getCommunications().forEach(c -> {
            String text;
            if (c.isSetText()) {
                text = c.getText();
            } else {
                text = "<no text>";
            }
            System.out.print(c.getId());
            System.out.print("\t");
            System.out.print(c.getUuid().getUuidString());
            System.out.print("\t");
            System.out.print(text);
            System.out.println();
        });
    }

    private static void toFile(RetrieveResults rr) {
        AtomicInteger count = new AtomicInteger(0);
        CompactCommunicationSerializer serializer = new CompactCommunicationSerializer();
        rr.getCommunications().forEach(c -> {
            String filename = c.getId() + ".concrete";
            try(OutputStream os = Files.newOutputStream(Paths.get(filename));
                BufferedOutputStream bos = new BufferedOutputStream(os);) {
                try {
                    bos.write(serializer.toBytes(c));
                    count.getAndIncrement();
                } catch (Exception e) {
                    LOGGER.warn("Caught exception serializing comm " + c.getId(), e);
                }
            } catch (IOException e) {
                LOGGER.warn("Caught IOException: {}", e.getMessage());
            }
        });
        System.out.println("Saved " + count.get() + " concrete files.");
    }

    private static class Opts {
        @Parameter(description = "space separated communication IDs", required = true)
        List<String> idList;

        @Parameter(names = {"--file", "-f"}, description = "Write the comms to file rather than stdout.")
        boolean file = false;

        @Parameter(help = true, names = {"--help", "-h"}, description = "Print the help message and exit.")
        boolean help = false;
    }

    public static void main(String[] args) {
        Opts opts = new Opts();
        JCommander jc = new JCommander(opts);
        jc.setProgramName("./retrieve.sh");
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

        try (RetrieveTool tool = new RetrieveTool();) {
            RetrieveResults rr = tool.retrieve(opts.idList);
            if (rr.isSetCommunications() && rr.getCommunicationsSize() > 0) {
                if (opts.file) {
                    RetrieveTool.toFile(rr);
                } else {
                    RetrieveTool.toStdout(rr);
                }
            } else {
                System.err.println("Did not retrieve any communications");
            }
        } catch (ServicesException e) {
            LOGGER.warn("Caught services exception: {}", e.getMessage());
        } catch (TException e) {
            LOGGER.warn("Caught TException:", e);
        }
    }
}
