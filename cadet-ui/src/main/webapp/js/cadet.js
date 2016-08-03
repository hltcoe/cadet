// The 'CADET' object provides methods for interacting with concrete-services
//
// None of the code in this file should manipulate the DOM.

/* globals AnnotationTaskType, FeedbackClient, RetrieverClient,
           ResultsServerClient, RetrieveRequest, SearchClient,
           SearchQuery, SearchType, Thrift
*/

var CADET = {
    // These variables are initialized by init()
    feedback: undefined,
    retrieve: undefined,  // alias for retriever
    retriever: undefined,
    search: undefined,
    results: undefined,

    // This variable is used to set the searchQuery.userId field
    userId: 'CADET User',

    registeredSearchResults: {},
    resultsWithFeedbackStarted: {},

    /** Takes a SearchResults and RetrieveResults object.  For each
     *  SearchResult (singular) object in SearchResults, add reference
     *  variables:
     *    - 'communication', which points to the Communication
     *        identified by searchResult.communicationId
     *    - 'sentence', which points to the Sentence identified
     *        by searchResult.sentenceId
     *
     * @param {SearchResults} searchResults
     * @param {RetrieveResults} retrieveResults
     */
    addReferencesToSearchResults: function(searchResults, retrieveResults) {
        var comm;
        var commIdToComm = {};
        for (var i = 0; i < retrieveResults.communications.length; i++) {
            comm = retrieveResults.communications[i];
            commIdToComm[comm.id] = comm;
            comm.addInternalReferences();
        }
        if (searchResults.searchResults) {
            for (var j = 0; j < searchResults.searchResults.length; j++) {
                var searchResult = searchResults.searchResults[j];
                comm = commIdToComm[searchResult.communicationId];
                searchResult.communication = comm;

                if (comm) {
                    // searchResult.sentence will be null if searchResult.sentenceId is not a valid Sentence UUID
                    searchResult.sentence = comm.getSentenceWithUUID(searchResult.sentenceId);
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

    /** Takes a SearchResult (singular) and a RetrieveResults, returns the Communication
     *  that is identified by the SearchResult and stored in the RetrieveResults.  Returns
     *  null if RetrieveResults did not contain the requested Communication
     *
     * @param {SearchResult} searchResult -
     * @param {RetrieveResults} retrieveResults - contains a list of Communications
     * @returns {Communication|null}
     */
    getCommunicationForSearchResult: function(searchResult, retrieveResults) {
        for (var i = 0; i < retrieveResults.communications.length; i++) {
            if (retrieveResults.communications[i].id === searchResult.communicationId) {
                return retrieveResults.communications[i];
            }
        }
        return null;
    },

    /** Get list of Communication IDs stored in SearchResults
     * @param {SearchResults} results
     * @returns {List} - A list of Communication ID strings
     */
    getCommunicationIdListFromSearchResults: function(results) {
        var idList = [];
        // iterates over searchResults and adds communication IDs to idList
        if (results.searchResults) {
            for (var i = 0; i < results.searchResults.length; i++){
                idList.push(results.searchResults[i].communicationId);
            }
        }
        // returns a list of communication IDs
        return idList;
    },

    init: function() {
        var feedback_transport = new Thrift.Transport('FeedbackServlet');
        var feedback_protocol = new Thrift.Protocol(feedback_transport);
        this.feedback = new FeedbackClient(feedback_protocol);

        var retriever_transport = new Thrift.Transport('RetrieverServlet');
        var retriever_protocol = new Thrift.Protocol(retriever_transport);
        this.retriever = new RetrieverClient(retriever_protocol);
        this.retrieve = this.retriever;

        var search_transport = new Thrift.Transport('SearchServlet');
        var search_protocol = new Thrift.Protocol(search_transport);
        this.search = new SearchClient(search_protocol);

        var results_transport = new Thrift.Transport('ResultsServer');
        var results_protocol = new Thrift.Protocol(results_transport);
        this.results = new ResultsServerClient(results_protocol);
    },

    /** Call the ResultsServer server's registerSearchResult()
     *  function IFF it has not called before for this particular
     *  SearchResults object.
     *
     * @param {SearchResults} searchResults
     */
    registerSearchResultWithGuard: function(searchResults) {
        if (!this.registeredSearchResults.hasOwnProperty(searchResults.uuid.uuidString)) {
            CADET.results.registerSearchResult(searchResults, AnnotationTaskType.NER);
            this.registeredSearchResults[searchResults.uuid.uuidString] = true;
        }
    },

    /** Retrieve Communications specified by Communication ID list
     * @param {List} idList - A list of Communication ID strings
     * @returns {RetrieveResults}
     */
    retrieveComms: function(idList) {
        // create args object to pass to RetrieveRequest
        var args = {};
        // list of communication IDs
        args.communicationIds = idList;
        // create RetrieveRequest object
        var request = new RetrieveRequest(args);

        // retrieve the communications
        var results = this.retriever.retrieve(request);
        return results;
    },

    /** Call the Feedback server's startFeedback() function IFF it has not
     *  called before for this particular SearchResults object.
     *
     * @param {SearchResults} searchResults
     */
    startFeedbackWithGuard: function(searchResults) {
        if (!this.resultsWithFeedbackStarted.hasOwnProperty(searchResults.uuid.uuidString)) {
            CADET.feedback.startFeedback(searchResults);
            this.resultsWithFeedbackStarted[searchResults.uuid.uuidString] = true;
        }
    }
};
