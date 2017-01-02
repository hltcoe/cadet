package edu.jhu.hlt.cadet.fetch;

import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.search.MockSearchProvider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.Section;
import edu.jhu.hlt.concrete.section.SectionFactory;
import edu.jhu.hlt.concrete.Sentence;
import edu.jhu.hlt.concrete.sentence.SentenceFactory;
import edu.jhu.hlt.concrete.TextSpan;
import edu.jhu.hlt.concrete.Tokenization;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.random.RandomConcreteFactory;
import edu.jhu.hlt.concrete.services.NotImplementedException;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.util.ConcreteException;
import edu.jhu.hlt.concrete.uuid.AnalyticUUIDGeneratorFactory;
import edu.jhu.hlt.concrete.uuid.AnalyticUUIDGeneratorFactory.AnalyticUUIDGenerator;
import edu.jhu.hlt.tift.Tokenizer;

/**
 * Generates mock communications for testing, debugging, and development
 */
public class MockFetchProvider implements FetchProvider {
    static private Map<String, String> comms;

    @Override
    public void init(Config config) {}

    @Override
    public void close() {}

    @Override
    public FetchResult fetch(FetchRequest request) throws ServicesException, TException {

        // this allows us to provide sentence Ids that support the mock search provider
        if (comms == null) {
            comms = MockSearchProvider.getMockCommsIds();
        }

        FetchResult results = new FetchResult();
        RandomConcreteFactory factory = new RandomConcreteFactory();
        NonsenseGenerator gen = NonsenseGenerator.getInstance();
        for (String commId : request.getCommunicationIds()) {
            Communication comm = factory.communication();
            AnalyticUUIDGeneratorFactory f = new AnalyticUUIDGeneratorFactory(comm);
            AnalyticUUIDGenerator uuidGen = f.create();

            String text = gen.makeHeadline();
            comm.setText(text);
            comm.setId(commId);

            TextSpan ts = new TextSpan(0, text.length());
            try {
                Section section = new SectionFactory(uuidGen).fromTextSpan(ts, "passage");
                Sentence sentence = new SentenceFactory(uuidGen).create();
                sentence.setTextSpan(ts);
                // set the sentence ID to match what's sent from mock search
                if (comms.containsKey(commId)) {
                    sentence.setUuid(new UUID(comms.get(commId)));
                }
                Tokenization tokenization = Tokenizer.WHITESPACE.tokenizeToConcrete(text, 0);
                sentence.setTokenization(tokenization);
                section.addToSentenceList(sentence);
                comm.addToSectionList(section);
            } catch (ConcreteException e) {
                throw new ServicesException();
            }

            results.addToCommunications(comm);
        }

        return results;
    }

    @Override
    public long getCommunicationCount() throws NotImplementedException, TException {
        throw new NotImplementedException("Mock does not support iteration");
    }

    @Override
    public List<String> getCommunicationIDs(long offset, long count) throws NotImplementedException, TException {
        throw new NotImplementedException("Mock does not support iteration");
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
