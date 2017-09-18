package edu.jhu.hlt.cadet.summarization;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.summarization.SummarizationService;;

public class SummarizationServlet extends TServlet {
    private static final long serialVersionUID = -9037326284809983170L;

    public SummarizationServlet() {
        super(new SummarizationService.Processor<>(ConfigManager.getInstance().getSummarizationHandler()), new TJSONProtocol.Factory());
    }
}