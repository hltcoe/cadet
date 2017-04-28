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
    var fetchRequest = new FetchRequest({'communicationIds': []});
    for (var i = 0; i < annotationUnitIdentifiers.length; i++) {
        fetchRequest.communicationIds.push(annotationUnitIdentifiers[i].communicationId)
        communicationIdToAUI[annotationUnitIdentifiers[i].communicationId] = annotationUnitIdentifiers[i];
    }
    var fetchResults = CADET.fetch.fetch(fetchRequest);

    for (var j = 0; j < fetchResults.communications.length; j++) {
        fetchResults.communications[j].annotationUnitIdentifier =
            communicationIdToAUI[fetchResults.communications[j].id];
    }

    return fetchResults.communications;
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

function updateDisplayedCommunications(comms) {
  var comm = comms[0];
  var tokenizationList = comm.getTokenizationsAsList();
  while (tokenizationList.length < 500) {
    var tok = Token;
    tok.text = "\t ";
    tokenizationList.push(tok);
  }
  var tokenization = tokenizationList[0];//[1];

  var tokenizationWidget = $('#tokenization').tokenizationWidget(
    tokenization, {whitespaceTokenization: true});
}

// Global variables
var COMMS = [];
var RESULTS_SERVER_SESSION_ID = null;

$(document).ready(function(){
   //$('#menu1 .businessSpecifics').remove();
    //$('#menu1 .businessSpecifics').remove();
    //var number=$("#selectStores option:selected").text();
     /*$(".add_event").click(function() {
       addButton(this);
     });

     $(".next_sentence").click(function() {
        $(".events")
        var url = window.location.href;
        var data = "test data";
        $.post(url+"next", data,
          function(data, status){
            alert("Data: " + data + "\nStatus: " + status);
          }, "JSON"
        );
        //alert("Next sentence to annotate");
      });

      var mouseIsIn;
      $('.outer').hover(function() {
         mouseIsIn = true;
      }, function() {
         mouseIsIn = false;
      });

      $('.outer').blur(function(){
         if (!mouseIsIn) {
             $('.outer').removeClass('on');
             $('.outer').addClass('off');
             $( '.outer' ).removeClass( "open" );
         }
      });

     function addButton(ele) {
        //alert("Add event!")
        if (ele.parentElement.childElementCount > 13) {
          ele.remove()
          return
        }
        /*ReactDOM.render(
          <EventTag />,
          //React.createElement(new EventTag, null),
          document.getElementById('content-events')
        );*/
        /*
        lastNode = ele.parentElement.children[ele.parentElement.childElementCount-2];
        clone = lastNode.cloneNode(true).childNodes[0];
        labels = clone.childNodes[1];
        for (i = 0; i < labels.length; i++) {
          labels.children[i].firstElementChild.name += "_event_"+ele.parentElement.childElementCount -1;
          labels.children[i].firstElementChild.checked = true;
        }
        checkedLabel = Math.round(Math.random() * 5);
        console.log(checkedLabel)
        labels.children[checkedLabel].firstElementChild.checked = true;
        ele.parentElement.insertBefore(clone, ele);*/
     //};

     /*$.getJSON('uz_data/uz_'+1+'_.concrete.json', function(commJSONData) {

       var comm = new Communication();
       comm.initFromTJSONProtocolObject(commJSONData);

       var tokenizationList = comm.getTokenizationsAsList();
       while (tokenizationList.length < 500) {
         var tok = Token;
         tok.text = "\t ";
         tokenizationList.push(tok);
       }
       var tokenization = tokenizationList[4];//[1];

       var tokenizationWidget = $('#tokenization').tokenizationWidget(
         tokenization, {whitespaceTokenization: true});

     });*/
    //$.getScript('../cadet.js', function() {
     alert("blah");
     CADET.init();
     var searchResultIdString = getUrlParameter('searchResultId');
     if (searchResultIdString) {
         var searchResultId = new UUID();
         searchResultId.uuidString = searchResultIdString;

         try {
             RESULTS_SERVER_SESSION_ID = CADET.results.startSession(searchResultId);
             var annotationUnitIdentifiers = CADET.results.getNextChunk(RESULTS_SERVER_SESSION_ID);
             COMMS = getNextCommunications(annotationUnitIdentifiers);
         }
         catch (error) {
             // TODO: Error handling
             throw error;
         }
     }
     else {
         // TODO: User-friendly error message about missing searchResultId
     }

     updateDisplayedCommunications(COMMS);
    //});
});

var funcs = [];
