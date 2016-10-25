package edu.jhu.hlt.cadet.store;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.access.StoreCommunicationService;

public class StoreServlet extends TServlet {
    private static final long serialVersionUID = -9037326284809983170L;

    public StoreServlet() {
        super(new StoreCommunicationService.Processor<>(ConfigManager.getInstance().getSenderHandler()), new TJSONProtocol.Factory());
    }
}
