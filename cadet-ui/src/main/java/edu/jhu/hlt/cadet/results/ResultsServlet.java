/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.results;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.services.results.ResultsServerService;

public class ResultsServlet extends TServlet {
    private static final long serialVersionUID = -6046925482249779649L;

    public ResultsServlet() {
        super(new ResultsServerService.Processor<>(ConfigManager.getInstance().getResultsHandler()), new TJSONProtocol.Factory());
    }
}
