package edu.jhu.hlt.cadet.summarization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.summarization.SummarizationCapability;
import edu.jhu.hlt.concrete.summarization.SummarizationService;
import edu.jhu.hlt.concrete.summarization.SummarizationRequest;
import edu.jhu.hlt.concrete.summarization.Summary;
import edu.jhu.hlt.concrete.summarization.SummarySourceType;
import edu.jhu.hlt.concrete.summarization.SummaryConcept;

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
