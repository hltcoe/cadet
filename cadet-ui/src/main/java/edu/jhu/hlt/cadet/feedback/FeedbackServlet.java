/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.feedback;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.server.TServlet;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.search.FeedbackService;;

public class FeedbackServlet extends TServlet {
    private static final long serialVersionUID = -5658698159812856486L;

    public FeedbackServlet() {
        super(new FeedbackService.Processor<>(ConfigManager.getInstance().getFeedbackHandler()), new TJSONProtocol.Factory());
    }
}
