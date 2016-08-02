package edu.jhu.hlt.concrete.retriever;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.access.Retriever;

public class RetrieverServlet extends TServlet {
    private static final long serialVersionUID = -9037326284809983170L;

    public RetrieverServlet() {
        super(new Retriever.Processor<>(ConfigManager.getInstance().getRetrieverHandler()), new TJSONProtocol.Factory());
    }
}
