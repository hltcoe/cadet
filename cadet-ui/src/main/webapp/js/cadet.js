// The 'CADET' object provides methods for interacting with concrete-services
//
// None of the code in this file should manipulate the DOM.

/* globals AnnotationTaskType, FeedbackServiceClient, FetchRequest,
           FetchCommunicationServiceClient,
           ResultsServerServiceClient, SearchClient, SearchQuery,
           SearchProxyServiceClient, SearchType,
           StoreCommunicationServiceClient, Thrift
*/

var CADET = {
    // These variables are initialized by init()
    feedback: undefined,
    fetch: undefined,
    search: undefined,
    search_proxy: undefined,
    store: undefined,
    results: undefined,

    // This variable is used to set the searchQuery.userId field
    userId: 'CADET User',

    registeredSearchResults: {},
    resultsWithFeedbackStarted: {},

    defaultSearchProviders: {},
    searchProvidersForSearchType: {},

    /** Takes a SearchResult and FetchResult object.  For each
     *  SearchResultItem object in SearchResult, add reference
     *  variables:
     *    - 'communication', which points to the Communication
     *        identified by searchResultItem.communicationId
     *    - 'sentence', which points to the Sentence identified
     *        by searchResultItem.sentenceId
     *
     * @param {SearchResult} searchResult
     * @param {FetchResult} fetchResult
     */
    addReferencesToSearchResultItems: function(searchResult, fetchResult) {
        var comm;
        var commIdToComm = {};
        for (var i = 0; i < fetchResult.communications.length; i++) {
            comm = fetchResult.communications[i];
            commIdToComm[comm.id] = comm;
            comm.addInternalReferences();
        }
        if (searchResult.searchResultItems) {
            for (var j = 0; j < searchResult.searchResultItems.length; j++) {
                var searchResultItem = searchResult.searchResultItems[j];
                comm = commIdToComm[searchResultItem.communicationId];
                searchResultItem.communication = comm;

                if (comm) {
                    // searchResultItem.sentence will be null if searchResultItem.sentenceId is not valid Sentence UUID
                    searchResultItem.sentence = comm.getSentenceWithUUID(searchResultItem.sentenceId);
                }
            }
        }
    },

    configureSearchProviders: function() {
        function updateSearchProviderFromLocalStorage(searchTypeString, providers) {
            if (localStorage.getItem('CADET.defaultSearchProviders.' + searchTypeString)) {
                if (providers.includes(localStorage.getItem('CADET.defaultSearchProviders.' + searchTypeString))) {
                    CADET.defaultSearchProviders[searchTypeString] = localStorage.getItem('CADET.defaultSearchProviders.' + searchTypeString);
                }
                else {
                    localStorage.removeItem('CADET.defaultSearchProviders.' + searchTypeString);
                }
            }
        }

        var providers = CADET.search_proxy.getProviders();
        var searchTypes = ['COMMUNICATIONS', 'ENTITY_MENTIONS', 'SENTENCES'];

        for (var s = 0; s < searchTypes.length; s++) {
            this.searchProvidersForSearchType[searchTypes[s]] = [];
            updateSearchProviderFromLocalStorage(searchTypes[s], providers);
        }

        for (var i = 0; i < providers.length; i++) {
            var capabilities = [];
            try {
                capabilities = CADET.search_proxy.getCapabilities(providers[i]);
            }
            catch (error) {
                // TODO: Don't ignore error
            }
            for (var j = 0; j < capabilities.length; j++) {
                for (var si = 0; si < searchTypes.length; si++) {
                    if (capabilities[j].type === SearchType[searchTypes[si]]) {
                        this.searchProvidersForSearchType[searchTypes[si]].push(providers[i]);

                        if (!this.defaultSearchProviders[searchTypes[si]]) {
                            this.defaultSearchProviders[searchTypes[si]] = providers[i];
                        }
                    }
                }
            }
        }
    },

    /** Creates a SearchQuery object from a search string
     * @param {String} searchString
     * @returns {SearchQuery}
     */
    createSearchQueryFromSearchString: function(searchString, queryName) {
        var i;

        var q = new SearchQuery();
        q.userId = this.userId;
        // Example query:
        // ?:"Where is brazil?" economics ?:who is the president of brazil" "dilma rousseff" ?:what is impeachment
        q.rawQuery = searchString;
        q.name = queryName;

        // Question markup: ?:"______"
        // To account for user error, any words found after ?: are considered part of a question until a closing quotation mark is reached.
        // Matches: ["?:\"Where is brazil?\"", "?:who is the president of brazil\"", "?:what is impeachment"]
        var questionMatches = searchString.match(/\?\:(\")?(([^\?\"])+(\s)*)+(\?)?(\")?/g);
        q.questions = [];
        // Filter out question markup (?:"") and add questions to SearchQuery.questions.
        if (questionMatches){
            for (i=0; i<questionMatches.length; i++){
                var filtered = questionMatches[i].replace(/^[\?]/g,'').replace(/[:\"]/g, '');
                q.questions.push(filtered);
            }
        }
        // Remove any questions, leaving only potential terms.
        // Example: "economics \"dilma rousseff\" "
        var termFiltered = searchString.replace(/\?\:(\")?(([^\?\"])+(\s)*)+(\?)?(\")?/g, "");
        // Match single words or phrases grouped with quotation marks.
        var termMatches = termFiltered.match(/\"([^\"])+"|[^\s\"]+/g);
        q.terms = [];
        // Remove any empty elements and add terms to SearchQuery.terms.
        if (termMatches){
            for (i=0; i < termMatches.length; i++){
                q.terms.push(termMatches[i].replace(/\"/g, ""));
            }
        }
        q.type = SearchType.SENTENCES;

        return q;
    },

    /** Takes a SearchResultItem and a FetchResult, returns the Communication
     *  that is identified by the SearchResultItem and stored in the FetchResult.
     *  Returns null if FetchResult did not contain the requested Communication
     *
     * @param {SearchResultItem} searchResultItem -
     * @param {FetchResult} fetchResult - contains a list of Communications
     * @returns {Communication|null}
     */
    getCommunicationForSearchResult: function(searchResultItem, fetchResult) {
        for (var i = 0; i < fetchResult.communications.length; i++) {
            if (fetchResult.communications[i].id === searchResultItem.communicationId) {
                return fetchResult.communications[i];
            }
        }
        return null;
    },

    /** Get list of Communication IDs stored in SearchResults
     * @param {SearchResult} result
     * @returns {List} - A list of Communication ID strings
     */
    getCommunicationIdListFromSearchResults: function(result) {
        var idList = [];
        // iterates over searchResultItems and adds communication IDs to idList
        if (result.searchResultItems) {
            for (var i = 0; i < result.searchResultItems.length; i++){
                idList.push(result.searchResultItems[i].communicationId);
            }
        }
        // returns a list of communication IDs
        return idList;
    },

    /** Get human readable string describing SearchType
     * @param {SearchType} searchType
     * @returns {String}
     */
    getSearchTypeString: function(searchType) {
        if (searchType === SearchType.COMMUNICATIONS) {
            return 'COMMUNICATIONS';
        }
        else if (searchType === SearchType.SECTIONS) {
            return 'SECTIONS';
        }
        else if (searchType === SearchType.SENTENCES) {
            return 'SENTENCES';
        }
        else if (searchType === SearchType.ENTITIES) {
            return 'ENTITIES';
        }
        else if (searchType === SearchType.ENTITY_MENTIONS) {
            return 'ENTITY_MENTIONS';
        }
        else if (searchType === SearchType.SITUATIONS) {
            return 'SITUATIONS';
        }
        else if (searchType === SearchType.SITUATION_MENTIONS) {
            return 'SITUATION_MENTIONS';
        }
    },

    init: function() {
        var feedback_transport = new Thrift.Transport('FeedbackServlet');
        var feedback_protocol = new Thrift.Protocol(feedback_transport);
        this.feedback = new FeedbackServiceClient(feedback_protocol);

        var fetch_transport = new Thrift.Transport('FetchServlet');
        var fetch_protocol = new Thrift.Protocol(fetch_transport);
        this.fetch = new FetchCommunicationServiceClient(fetch_protocol);

        var results_transport = new Thrift.Transport('ResultsServer');
        var results_protocol = new Thrift.Protocol(results_transport);
        this.results = new ResultsServerServiceClient(results_protocol);

        var search_proxy_transport = new Thrift.Transport('SearchProxyServlet');
        var search_proxy_protocol = new Thrift.Protocol(search_proxy_transport);
        this.search_proxy = new SearchProxyServiceClient(search_proxy_protocol);

        var store_transport = new Thrift.Transport('StoreServlet');
        var store_protocol = new Thrift.Protocol(store_transport);
        this.store = new StoreCommunicationServiceClient(store_protocol);

        this.configureSearchProviders();
    },

    /** Call the ResultsServer server's registerSearchResult()
     *  function IFF it has not called before for this particular
     *  SearchResults object.
     *
     * @param {SearchResult} searchResult
     */
    registerSearchResultWithGuard: function(searchResult) {
        if (!this.registeredSearchResults.hasOwnProperty(searchResult.uuid.uuidString)) {
            CADET.results.registerSearchResult(searchResult, AnnotationTaskType.NER);
            this.registeredSearchResults[searchResult.uuid.uuidString] = true;
        }
    },

    /** Fetch Communications specified by Communication ID list
     * @param {List} idList - A list of Communication ID strings
     * @returns {FetchResult}
     */
    fetchComms: function(idList) {
        // create args object to pass to FetchRequest
        var args = {};
        // list of communication IDs
        args.communicationIds = idList;
        // create FetchRequest object
        var request = new FetchRequest(args);

        // fetch the communications
        var results = this.fetch.fetch(request);
        return results;
    },

    /** Set default search provider for specified SearchType
     * @param {String} searchTypeString - e.g. 'COMMUNICATIONS', 'ENTITY_MENTIONS'
     * @param {String} providerName
     */
    setDefaultSearchProvider: function(searchTypeString, providerName) {
        this.defaultSearchProviders[searchTypeString] = providerName;
        localStorage.setItem('CADET.defaultSearchProviders.' + searchTypeString, providerName);
    },

    /** Call the Feedback server's startFeedback() function IFF it has not
     *  called before for this particular SearchResult object.
     *
     * @param {SearchResult} searchResult
     */
    startFeedbackWithGuard: function(searchResult) {
        if (!this.resultsWithFeedbackStarted.hasOwnProperty(searchResult.uuid.uuidString)) {
            CADET.feedback.startFeedback(searchResult);
            this.resultsWithFeedbackStarted[searchResult.uuid.uuidString] = true;
        }
    }
};
