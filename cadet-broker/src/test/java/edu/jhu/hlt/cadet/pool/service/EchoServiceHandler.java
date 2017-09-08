package edu.jhu.hlt.cadet.pool.service;

import org.apache.thrift.TException;

public class EchoServiceHandler implements EchoService.Iface {

    @Override
    public String echo(String text) throws TException {
        return text;
    }

}
