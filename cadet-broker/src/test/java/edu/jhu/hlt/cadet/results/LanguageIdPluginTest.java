/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.results;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.thrift.TException;
import org.junit.Test;

import edu.jhu.hlt.cadet.fetch.FetchProvider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.LanguageIdentification;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;
import edu.jhu.hlt.concrete.services.ServicesException;

public class LanguageIdPluginTest {

    @Test
    public void test() throws ServicesException, TException {
        // prepare the mock data
        LanguageIdentification li = new LanguageIdentification();
        li.putToLanguageToProbabilityMap("nld", 0.5);
        li.putToLanguageToProbabilityMap("deu", 0.4);
        li.putToLanguageToProbabilityMap("pol", 0.7);
        li.putToLanguageToProbabilityMap("rus", 0.2);
        Communication comm = new Communication();
        comm.addToLidList(li);
        FetchResult result = new FetchResult();
        result.addToCommunications(comm);
        FetchProvider fetcher = mock(FetchProvider.class);
        when(fetcher.fetch(any())).thenReturn(result);

        // fake search result
        SearchResultItem sri = new SearchResultItem();
        sri.setCommunicationId("test");
        SearchResult searchResults = new SearchResult();
        searchResults.addToSearchResultItems(sri);

        ResultsPlugin plugin = new LanguageIdPlugin();
        plugin.setFetchProvider(fetcher);

        assertTrue(plugin.process(searchResults));
        assertEquals("pol", searchResults.getLang());

        plugin.close();
    }

}
