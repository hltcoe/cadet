package edu.jhu.hlt.cadet.search;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.search.SearchService;

public class SearchServlet extends TServlet {
    private static final long serialVersionUID = 998247877653144034L;

    public SearchServlet() {
        super(new SearchService.Processor<>(ConfigManager.getInstance().getSearchHandler()), new TJSONProtocol.Factory());
    }
}
