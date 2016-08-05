package edu.jhu.hlt.cadet.send;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.access.Sender;

public class SenderServlet extends TServlet {
    private static final long serialVersionUID = -9037326284809983170L;

    public SenderServlet() {
        super(new Sender.Processor<>(ConfigManager.getInstance().getSenderHandler()), new TJSONProtocol.Factory());
    }
}
