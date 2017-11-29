/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.pool.service;

import org.apache.thrift.TException;

public class EchoServiceHandler implements EchoService.Iface {

    @Override
    public String echo(String text) throws TException {
        return text;
    }

}
