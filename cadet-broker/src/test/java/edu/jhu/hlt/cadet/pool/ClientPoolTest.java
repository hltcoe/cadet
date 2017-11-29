/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.pool;

import static org.junit.Assert.*;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.jhu.hlt.cadet.pool.service.EchoServiceHandler;
import edu.jhu.hlt.cadet.pool.service.EchoService;
import edu.jhu.hlt.cadet.pool.service.EchoService.Processor;

public class ClientPoolTest {
    private static final int port = 9494;

    @BeforeClass
    public static void setUp() throws InterruptedException, TTransportException {
        TServerTransport serverTransport = new TServerSocket(port);

        Args processor = new TThreadPoolServer.Args(serverTransport)
                    .inputTransportFactory(new TFramedTransport.Factory())
                    .protocolFactory(new TCompactProtocol.Factory())
                    .outputTransportFactory(new TFramedTransport.Factory())
                    .processor(new Processor<>(new EchoServiceHandler()));
        TThreadPoolServer server = new TThreadPoolServer(processor);
        new Thread(server::serve).start();
    }

    @Test
    public void test() throws TException {
        PoolConfig pc = new PoolConfig(10, 1000L);
        ServiceConfig sc = new ServiceConfig("localhost", port);
        ClientPool<EchoService.Client> clientPool = new ClientPool<EchoService.Client>(pc, sc,
                        transport -> new EchoService.Client(new TCompactProtocol(transport)));

        EchoService.Client client = clientPool.borrowClient();
        String ans = client.echo("hello");
        assertEquals("hello", ans);
    }

}
