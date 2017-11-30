/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.fetch;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.access.FetchCommunicationService;;

public class FetchServlet extends TServlet {
    private static final long serialVersionUID = -9037326284809983170L;

    public FetchServlet() {
        super(new FetchCommunicationService.Processor<>(ConfigManager.getInstance().getFetchHandler()), new TJSONProtocol.Factory());
    }
}
