/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.summarization;

import java.util.List;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.Provider;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.summarization.SummarizationCapability;
import edu.jhu.hlt.concrete.summarization.SummarizationRequest;
import edu.jhu.hlt.concrete.summarization.Summary;

public interface SummarizationProvider extends Provider {
    /**
     * Get a summary of some text data
     *
     * @param query A SummarizationRequest object with length of desired summary and text data to be summarized
     * @return a Summary object
     */
    public Summary summarize(SummarizationRequest query) throws ServicesException, TException;

    /**
     * Get a list of the languages and summarization types supported by this service
     */
    public List<SummarizationCapability> getCapabilities() throws ServicesException, TException;

    /**
     * Is the service alive?
     */
    public boolean alive() throws TException;

    /**
     * Get information about the fetch provider
     */
    public ServiceInfo about() throws TException;
}
