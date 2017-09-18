package edu.jhu.hlt.cadet.summarization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import org.apache.thrift.TException;

import edu.jhu.hlt.cadet.summarization.SummarizationProvider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.services.NotImplementedException;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.summarization.SummarizationCapability;
import edu.jhu.hlt.concrete.summarization.SummarizationService;
import edu.jhu.hlt.concrete.summarization.SummarizationRequest;
import edu.jhu.hlt.concrete.summarization.Summary;
import edu.jhu.hlt.concrete.summarization.SummarySourceType;
import edu.jhu.hlt.concrete.summarization.SummaryConcept;

public class SummarizationHandler implements SummarizationService.Iface {
    private static Logger logger = LoggerFactory.getLogger(SummarizationHandler.class);

    private SummarizationProvider summarizationProvider = null;

    public SummarizationHandler() {}

    /**
     * Initialize the handler - must be called before any other methods
     *
     * @param provider
     */
    public void init(SummarizationProvider provider) {
        summarizationProvider = provider;
    }

    @Override
    public Summary summarize(SummarizationRequest query) throws ServicesException, TException {
        logger.info("summarizationHandler.summarize()");
	return summarizationProvider.summarize(query);
    }

    @Override
    public List<SummarizationCapability> getCapabilities() throws ServicesException, TException {
        logger.info("summarizationHandler.getCapabilities()");
	return summarizationProvider.getCapabilities();
    }

    @Override
    public ServiceInfo about() throws TException {
        logger.info("summarizationHandler.about()");
        if (summarizationProvider != null) {
            return summarizationProvider.about();
        } else {
            return new ServiceInfo("N/A", "0");
        }
    }

    @Override
    public boolean alive() throws TException {
        logger.info("summarizationHandler.alive()");
        if (summarizationProvider != null) {
            return summarizationProvider.alive();
        } else {
            return false;
        }
    }
}
