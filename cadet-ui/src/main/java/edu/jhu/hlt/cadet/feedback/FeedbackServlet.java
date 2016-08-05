package edu.jhu.hlt.cadet.feedback;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.search.Feedback;

public class FeedbackServlet extends TServlet {
    private static final long serialVersionUID = -5658698159812856486L;

    public FeedbackServlet() {
        super(new Feedback.Processor<>(ConfigManager.getInstance().getFeedbackHandler()), new TJSONProtocol.Factory());
    }
}
