/*
 * Copyright 2012-2017 Johns Hopkins University HLTCOE. All rights reserved.
 * This software is released under the 2-clause BSD license.
 * See LICENSE in the project root directory.
 */
package edu.jhu.hlt.cadet.results;

import java.util.Map;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.fetch.FetchProvider;
import edu.jhu.hlt.concrete.Communication;
import edu.jhu.hlt.concrete.access.FetchRequest;
import edu.jhu.hlt.concrete.access.FetchResult;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;

/**
 * Sets the language of the search results if not set.
 *
 * Uses the language of the top search result.
 */
public class LanguageIdPlugin implements ResultsPlugin {
    private static Logger logger = LoggerFactory.getLogger(LanguageIdPlugin.class);

    private FetchProvider fetchProvider = null;

    @Override
    public void init(Config config) {}

    @Override
    public void close() {}

    @Override
    public void setFetchProvider(FetchProvider provider) {
        fetchProvider = provider;
    }

    @Override
    public boolean process(SearchResult result) {
        if (fetchProvider == null) {
            return true;
        }

        if (!result.isSetLang() && result.getSearchResultItemsSize() > 0) {
            SearchResultItem item = result.getSearchResultItems().get(0);
            String commId = item.getCommunicationId();
            FetchRequest request = new FetchRequest();
            request.addToCommunicationIds(commId);
            try {
                FetchResult fetchResult = fetchProvider.fetch(request);
                if (fetchResult.getCommunicationsSize() == 1) {
                    Communication comm = fetchResult.getCommunications().get(0);
                    if (comm.getLidListSize() > 0) {
                        Map<String, Double> map =
                                        comm.getLidList().get(0).getLanguageToProbabilityMap();
                        if (map.size() > 0) {
                            // sort map from smallest to largest and get last element
                            String lang = map.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue())
                                .reduce((a, b) -> b).get().getKey();
                            result.setLang(lang);
                        }
                    }
                }
            } catch (TException e) {
                logger.warn("Unable to retrieve a communication", e);
            }
        }

        return true;
    }

}
