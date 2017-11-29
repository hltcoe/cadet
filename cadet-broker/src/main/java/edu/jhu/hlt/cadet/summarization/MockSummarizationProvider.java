/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.summarization;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.summarization.SummarizationCapability;
import edu.jhu.hlt.concrete.summarization.SummarizationRequest;
import edu.jhu.hlt.concrete.summarization.Summary;
import edu.jhu.hlt.concrete.summarization.SummarySourceType;

/**
 * Generates mock summarizes for testing, debugging, and development
 */
public class MockSummarizationProvider implements SummarizationProvider {

    @Override
    public void init(Config config) {}

    @Override
    public void close() {}

    @Override
    public Summary summarize(SummarizationRequest query) throws ServicesException, TException {
        Summary summary = new Summary();

        // TODO: Populate summary.summaryCommunication and summary.concepts

        return summary;
    }

    public List<SummarizationCapability> getCapabilities() throws ServicesException, TException {
        List<SummarizationCapability> capabilities = new ArrayList<SummarizationCapability>();

        SummarizationCapability documentCapability = new SummarizationCapability();
        documentCapability.setLang("eng");
        documentCapability.setType(SummarySourceType.DOCUMENT);
        capabilities.add(documentCapability);

        return capabilities;
    }

    @Override
    public boolean alive() throws TException {
        return true;
    }

    @Override
    public ServiceInfo about() throws TException {
        return new ServiceInfo(this.getClass().getSimpleName(), "1.0.0");
    }

}
