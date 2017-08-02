package edu.jhu.hlt.cadet.pool;

import java.util.function.Function;

import org.apache.thrift.TServiceClient;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ClientPool<T extends TServiceClient> {
    private T client;

    public ClientPool(ServiceConfig service, Function<TTransport, T> factory) {
        //this.client = client;
        TSocket socket = new TSocket(service.getHost(), service.getPort());
        TTransport transport = new TFramedTransport(socket, Integer.MAX_VALUE);
        try {
            transport.open();
        } catch (TTransportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.client = factory.apply(transport);
    }

    public T getClient() {
        return client;
    }
}
