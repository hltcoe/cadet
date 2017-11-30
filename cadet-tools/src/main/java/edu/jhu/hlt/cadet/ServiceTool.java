/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import edu.jhu.hlt.concrete.services.Service;
import edu.jhu.hlt.concrete.services.ServiceInfo;

public class ServiceTool implements AutoCloseable {
    private TTransport transport;
    private TCompactProtocol protocol;
    private Service.Client client;

    public ServiceTool(String host, int port) {
        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new Service.Client(protocol);
    }

    public boolean alive() throws TException {
        transport.open();
        boolean ans = client.alive();
        transport.close();
        return ans;
    }

    public ServiceInfo about() throws TException {
        transport.open();
        ServiceInfo info = client.about();
        transport.close();
        return info;     
    }

    @Override
    public void close() {
        if (transport.isOpen()) {
            transport.close();
        }
    }

    public static void main(String[] args) {
        String method = "alive";
        if (args.length >= 1) {
            method = args[0];
        }
        if (args.length != 3) {
            System.err.println("Usage: ./" + method + ".sh <host> <port>");
            return;
        }

        String host = args[1];
        int port = Integer.valueOf(args[2]);
        try (ServiceTool tool = new ServiceTool(host, port);) {
            if (method.equals("alive")) {
                try {
                    tool.alive();
                    System.out.println("service is alive");
                } catch (TException e) {
                    System.err.println(e.getMessage());
                }
            } else {
                try {
                    ServiceInfo info = tool.about();
                    System.out.println("Name: " + info.getName());
                    System.out.println("Version: " + info.getVersion());
                    if (info.getDescription() != null) {
                        System.out.println("Description: " + info.getDescription());
                    }
                } catch (TException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

}
