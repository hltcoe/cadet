/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.learn;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import edu.jhu.hlt.concrete.learn.ActiveLearnerClientService;
import edu.jhu.hlt.concrete.learn.ActiveLearnerClientService.Iface;

public class SortReceiverServer implements Runnable {

    private final TServer server;
    private final TNonblockingServerTransport transport;
    private final int port;

    public SortReceiverServer(ActiveLearnerClientService.Iface impl, int port) throws TTransportException {
        this.port = port;
        transport = new TNonblockingServerSocket(port);
        final TNonblockingServer.Args args = new TNonblockingServer.Args(transport);
        args.protocolFactory(new TCompactProtocol.Factory());
        final TFramedTransport.Factory transFactory = new TFramedTransport.Factory(Integer.MAX_VALUE);
        args.transportFactory(transFactory);
        ActiveLearnerClientService.Processor<Iface> proc = new ActiveLearnerClientService.Processor<>(impl);
        args.processorFactory(new TProcessorFactory(proc));
        args.maxReadBufferBytes = Long.MAX_VALUE;
        server = new TNonblockingServer(args);
    }

    @Override
    public void run() {
        server.serve();
    }

    public void close() {
        server.stop();
        transport.close();
    }

    public int getPort() {
        return port;
    }
}
