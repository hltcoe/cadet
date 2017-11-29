function assert(condition, message) {
    if (!condition) {
        message = message || "Assertion failed";
        if (typeof Error !== "undefined") {
            throw new Error(message);
        }
        throw message; // Fallback
    }
}

function addTagTextBox(tagIndex) {
    $('#tag_type_list').append(
        $('<li>').addClass('tag_text_li')
                 .append(
                     $('<input>').addClass('tag_text_input')
                                 .attr('type', 'text')
                                 .change({'tagSet': tagSet, 'tagIndex':tagIndex}, function(event) {
                                     event.data.tagSet[event.data.tagIndex] = $(this).val();
                                 })
                                 .val(tagSet[tagIndex])));
}

/** Takes an AnnotationUnitIdentifier list, returns a list of Communications.
 *  Adds an 'annotationUnitIdentifier' field to each retrieved Communication.
 *
 * @param {AnnotationUnitIdentifier list} annotationUnitIdentifiers
 * @returns {Communication list}
 */
function getNextCommunications(annotationUnitIdentifiers) {
    if (annotationUnitIdentifiers.length === 0) {
        return [];
    }

    var communicationIdToAUI = {};
    var sentenceUUIDs = []
    var commIdsSet = new Set();
    var loc2commId = {};
    var fetchRequest = new FetchRequest({'communicationIds': []});
    for (var i = 0; i < annotationUnitIdentifiers.length; i++) {
        if (!commIdsSet.has(annotationUnitIdentifiers[i].communicationId)) {
          fetchRequest.communicationIds.push(annotationUnitIdentifiers[i].communicationId);
          commIdsSet.add(annotationUnitIdentifiers[i].communicationId);
        }
        loc2commId[i] = annotationUnitIdentifiers[i].communicationId;
        sentenceUUIDs.push(annotationUnitIdentifiers[i].sentenceId);
        communicationIdToAUI[annotationUnitIdentifiers[i].communicationId] = annotationUnitIdentifiers[i];
    }
    var fetchResults = CADET.fetch.fetch(fetchRequest);

    var fetchedComms = {}
    for (var j = 0; j < fetchResults.communications.length; j++) {
      fetchedComms[fetchResults.communications[j].id] =   fetchResults.communications[j];
    }

    fetchResults.communications = new Array(Object.keys(loc2commId).length);
    for (var j = 0; j < fetchResults.communications.length; j++) {
      fetchResults.communications[j] = fetchedComms[loc2commId[j]];
    }


    for (var j = 0; j < fetchResults.communications.length; j++) {
        fetchResults.communications[j].annotationUnitIdentifier =
            communicationIdToAUI[fetchResults.communications[j].id];
    }

    assert(fetchResults.communications.length == sentenceUUIDs.length, "Number of sentences and communications match")
    return [fetchResults.communications, sentenceUUIDs];
}

function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
}

function saveCommsToFormData() {
    // HACK: tokentagging_ui.js calls saveCommsToFormData() whenever a token tag changes.
    // TODO: Refactor tokentagging_ui.js (which also exists in the NER_HIT repository)
}

function updateDisplayedCommunications(comms, sents) {
    $('#tokenization_list').empty();

    for (var commIndex = 0; commIndex < comms.length; commIndex++) {
        var comm = comms[commIndex];
        var sentUUID = sents[commIndex];

        var tokensDiv = $('<div>').attr('id', 'comm_' + commIndex + '_tokens');
        var tokenTagInputsDiv = $('<div>').attr('id', 'comm_' + commIndex + '_ne_inputs');

        //var tokenization = getFirstTokenization(comm);
        var sentence = comm.getSentenceWithUUID(sentUUID)
        var tokenization = sentence.tokenization

        var tokenTaggingIndex = checkForPreviousNERTags(tokenization);
        if (tokenization) {
            // If there is a previous NER TokenTagging, it is duplicated.
            if (tokenTaggingIndex !== false) {
                var tokenTagging = duplicateTokenTagging(tokenization, tokenTaggingIndex, "NER");
            }
            // If there is not a previous NER TokenTagging, a new one is created
            // and intializes with "O" tags for all tokens.
            else {
                var tokenTagging = createTokenTagging("NER");
                setAllTokenTagsToO(tokenization, tokenTagging);
            }
            addTokenTagging(tokenization, tokenTagging);
            for (var tokenIndex in tokenization.tokenList.tokenList) {
                var tokenTag = getTaggedTokenWithIndex(tokenTagging,tokenIndex).tag;
                // placeholder
                var foo = tagSet.indexOf(tokenTag.split("-").pop());
                var token_span = $('<span>')
                    .addClass(returnsCSSClassForTag(tokenTagging, tokenIndex))
                    .attr('data-tag-type-index', foo)
                    .attr('id', 'comm_' + commIndex + '_token_' + tokenIndex)
                    .css("background-color", tagColorPairs[foo])
                    .click({'commIndex': commIndex,
                            'tokenization': tokenization,
                            'tokenIndex': tokenIndex,
                            'tokenTagging': tokenTagging},
                           updateTagForNECallback)
                    .html(tokenization.tokenList.tokenList[tokenIndex].text + '<span class="tokenTag">(' + tokenTag +')</span>');
                tokensDiv.append(token_span);
                var token_whitespace = $('<span>').text(' ');
                tokensDiv.append(token_whitespace);

                // For each token, create an (initially empty) container where an
                // NE label control can be displayed.
                tokenTagInputsDiv.append(
                    $('<div>')
                        .attr('id', 'comm_' + commIndex + '_token_' + tokenIndex + '_ne_input_container'));
                // Add NE label control for all "B" tokens and intializes with the correct value based on its tag.
                if (tokenTag.charAt(0)=="B") {
                    addNEInputControl(commIndex, tokenization, tokenIndex, tokenTagging, tagSet);
                }
            }
            var tokenTaggingDiv = $('<div>').addClass('token_tagging_container')
                                            .append(tokensDiv)
                                            .append(tokenTagInputsDiv);
            $('#tokenization_list').append(tokenTaggingDiv);
        }
    }
}


// Global variables
var COMMS = [];
var SENTS = [];
var RESULTS_SERVER_SESSION_ID = null;

$(document).ready(function(){
    CADET.init();


    for (var i = 0; i < tagSet.length; i++) {
        addTagTextBox(i);
    }
    $('#tag_type_list').after(
        $('<span>').addClass('glyphicon glyphicon-plus')
                   .css('cursor', 'pointer')
                   .on('click', function(event) {
                       tagSet.push('');
                       addTagTextBox(tagSet.length - 1);
                   }));

    var searchResultIdString = getUrlParameter('searchResultId');
    if (searchResultIdString) {
        var searchResultId = new UUID();
        searchResultId.uuidString = searchResultIdString;

        try {
            RESULTS_SERVER_SESSION_ID = CADET.results.startSession(searchResultId);
            var annotationUnitIdentifiers = CADET.results.getNextChunk(RESULTS_SERVER_SESSION_ID);
            var res = getNextCommunications(annotationUnitIdentifiers);
            COMMS = res[0]
            SENTS = res[1]
        }
        catch (error) {
            // TODO: Error handling
            throw error;
        }
    }
    else {
        // TODO: User-friendly error message about missing searchResultId
    }

    $('#save_button').on('click', function(event) {
        try {
            if (COMMS && COMMS.length > 0) {
                for (var i = 0; i < COMMS.length; i++) {
                    CADET.results.submitAnnotation(
                        RESULTS_SERVER_SESSION_ID,
                        // The .annotationUnitIdentifier field is added by getNextCommunications()
                        COMMS[i].annotationUnitIdentifier,
                        COMMS[i]);
                }
            }
            var annotationUnitIdentifiers = CADET.results.getNextChunk(RESULTS_SERVER_SESSION_ID);
            var res = getNextCommunications(annotationUnitIdentifiers);
            COMMS = res[0]
            SENTS = res[1]
            if (COMMS.length > 0) {
                updateDisplayedCommunications(COMMS, SENTS);
            }
            else {
                location.replace("results.html");
            }
        }
        catch (error) {
        }
    });

    updateDisplayedCommunications(COMMS, SENTS);
});
