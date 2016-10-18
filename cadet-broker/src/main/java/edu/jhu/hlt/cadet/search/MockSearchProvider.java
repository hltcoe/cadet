package edu.jhu.hlt.cadet.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;

import com.typesafe.config.Config;

import edu.jhu.hlt.concrete.AnnotationMetadata;
import edu.jhu.hlt.concrete.UUID;
import edu.jhu.hlt.concrete.search.SearchCapability;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.search.SearchType;
import edu.jhu.hlt.concrete.services.ServiceInfo;
import edu.jhu.hlt.concrete.services.ServicesException;
import edu.jhu.hlt.concrete.uuid.AnalyticUUIDGeneratorFactory;

/**
 * Returns fake IDs to a search query
 */
public class MockSearchProvider implements SearchProvider {

    @Override
    public void init(Config config) {}

    @Override
    public void close() {}

    @Override
    public SearchResult search(SearchQuery searchQuery) throws ServicesException, TException {
        return createMockData(searchQuery);
    }

    private SearchResult createMockData(SearchQuery query) {
        AnalyticUUIDGeneratorFactory f = new AnalyticUUIDGeneratorFactory();
        AnalyticUUIDGeneratorFactory.AnalyticUUIDGenerator gen = f.create();

        SearchResult results = new SearchResult();

        Map<String, String> comms = new HashMap<String, String>();
        comms.put("99508872582148096", "74c72918-6825-25a1-b4a1-000034f6aa82");
        comms.put("100023698194575360", "e9362194-4e5e-33ec-1c88-00000744e98c");
        comms.put("103308526105014272", "08754e53-aa72-fcd8-e8a5-00001f08def3");
        comms.put("103308534493626369", "da94bceb-fd22-5c65-cd69-000073cf2fd6");
        comms.put("103308534502006784", "c02cb5d3-821b-93ec-8ee0-00004b3de2ed");
        comms.put("103308534502010880", "23e63560-5081-b518-9fce-00003ac08cfa");
        comms.put("103308534514585600", "f566af93-605c-0b45-1545-000061fdfc0b");
        comms.put("103308534510387201", "c8debea6-e889-5f34-e7e5-000029ae176f");
        comms.put("103308530320281600", "959c3597-1dfc-2721-1ade-00002e90c015");
        comms.put("103308530316099584", "baefca4f-5eab-475b-b7da-0000292a92f4");
        comms.put("103308534485237762", "dedc4f62-69af-7c54-d3c8-00000802aa91");
        comms.put("103308534485237760", "4e67542a-5bc6-ab8a-3311-00007504f2e7");
        comms.put("103308534489415680", "145b10ec-e73f-960a-a79e-0000072ac3a6");

        AnnotationMetadata metadata = new AnnotationMetadata();
        metadata.setTool("MockSearchProvider");
        results.setMetadata(metadata);
        results.setUuid(gen.next());
        results.setSearchQuery(query);

        for (Map.Entry<String, String> comm : comms.entrySet()) {
            SearchResultItem result = new SearchResultItem();
            if (query.getType() == SearchType.SENTENCES) {
                result.setSentenceId(new UUID(comm.getValue()));
            }
            result.setCommunicationId(comm.getKey());
            result.setScore(0.0);
            results.addToSearchResultItems(result);
        }

        return results;
    }

    @Override
    public boolean alive() throws TException {
        return true;
    }

    @Override
    public ServiceInfo about() throws TException {
        return new ServiceInfo(this.getClass().getSimpleName(), "1.0.0");
    }

    @Override
    public List<SearchCapability> getCapabilities() throws ServicesException {
	List<SearchCapability> capabilities = new ArrayList<SearchCapability>();

	SearchCapability communicationsCapability = new SearchCapability();
	communicationsCapability.setLang("eng");
	communicationsCapability.setType(SearchType.COMMUNICATIONS);
	capabilities.add(communicationsCapability);

	SearchCapability sentencesCapability = new SearchCapability();
	sentencesCapability.setLang("eng");
	sentencesCapability.setType(SearchType.SENTENCES);
	capabilities.add(sentencesCapability);

	return capabilities;
    }

    @Override
    public List<String> getCorpora() throws ServicesException {
	List<String> corpora = new ArrayList<String>();
	corpora.add("MockCorpora");
	return corpora;
    }
}
