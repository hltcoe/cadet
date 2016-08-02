package edu.jhu.hlt.concrete.results;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.services.results.ResultsServer;

public class ResultsServlet extends TServlet {
    private static final long serialVersionUID = -6046925482249779649L;

    public ResultsServlet() {
        super(new ResultsServer.Processor<>(ConfigManager.getInstance().getResultsHandler()), new TJSONProtocol.Factory());
    }
}
