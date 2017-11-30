/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.search;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.search.SearchProxyService;

public class SearchProxyServlet extends TServlet {
    private static final long serialVersionUID = -3045530669249298191L;

    public SearchProxyServlet() {
        super(new SearchProxyService.Processor<>(ConfigManager.getInstance().getSearchProxyHandler()), new TJSONProtocol.Factory());
    }
}
