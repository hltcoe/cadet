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

function getMaxSents() {
  var sents = 0;
  for (var i = 0; i < COMMS.length; i++) {
    sents += COMMS[i].sectionList[0].sentenceList.length;
  }
  return sents;
}

function addEventToComm(comm, sentNum) {
  for (var i = 0; i < eventTag.state.eventNums - 1; i++) {
    // add situationMentions
  }
  comm.situationMentionSetList[sentNum].mentionList[0].situationKind = eventTag.state.eventType; // + ":::" + eventTag.state.ordinalRating;
  comm.situationMentionSetList[sentNum].mentionList[0].intensity = parseInt(eventTag.state.ordinalRating) / 3
}

// Global variables
var COMMS = [];
var currSituationMentionSet = null;
var currSitationMention = null;
var RESULTS_SERVER_SESSION_ID = null;
var EVENT_MAP = {};
var params={};
var submitButton = null;

var funcs = [];

var events = {}

function updateEventMapping(key) {
  EVENT_MAP[key] = eventTag.state.eventType + ":::" + eventTag.state.ordinalRating;
}

function updateDisplayedCommunications(comm, sentNum, first) {
  //change_text(comms[0])
  //var comm = comms[0];
  /*var tokenizationList = comm.getTokenizationsAsList();
  while (tokenizationList.length < 500) {
    var tok = Token;
    tok.text = "\t ";
    tokenizationList.push(tok);
  }

  if (!first) {
    document.getElementById("tokenization").innerHTML = '';
  }

  var tokenization = comm.sectionList[0].sentenceList[0].tokenization;//.tokenList;//tokenizationList[0];//[1];


  var tokenizationWidget = $('#tokenization').tokenizationWidget(
    tokenization, {whitespaceTokenization: true}); */
  var sentToDisplay = 0;
  if (!first) {
    document.getElementById("tokenization").innerHTML = '';
    if (submitButton != null) {
      sentToDisplay = submitButton.state.currSentInComm;
      eventTag.setState({eventType: "No Event", ordinalRating: '3'});
    }
  } else if (submitButton != null){
      submitButton.setState({
        maxSents: getMaxSents(),
        totalComms: COMMS.length,
        totalSentsLabeled: 0,
        currComm: 0,
        totalSentsInCurrComm: comm.sectionList[0].sentenceList.length,
        currSentInComm: 0,
      });
      console.log("sents in Curr Comm: " + submitButton.state.totalSentsInCurrComm)
      console.log("maxSents: " + submitButton.state.maxSents);
      console.log("totalComs: " + submitButton.state.totalComms);
      sentToDisplay = submitButton.state.currSentInComm;
  }

  var tokenization = comm.sectionList[0].sentenceList[sentToDisplay].tokenization;//.tokenList;//tokenizationList[0];//[1];
  var tokenizationWidget = $('#tokenization').tokenizationWidget(
    tokenization, {whitespaceTokenization: true});
  //currSituationMention.tokens = new TokenRefSequence();
  //currSituationMention.tokens =
}

class CommunicationContainer extends React.Component {
  constructor() {
    super();
    this.props = {
        fileNumber: 1
    };
    //this.change_text = this.change_text.bind(this);
  }

  /*change_text(i) {
    //$.getJSON('uz_data/uz_'+this.props.fileNumber+'_.concrete.json', function(commJSONData) {

      //var comm = new Communication();
      //comm.initFromTJSONProtocolObject(commJSONData);

    var tokenization = tokenizationList[i];//[1];
    document.getElementById("tokenization").innerHTML = '';

    var tokenizationWidget = $('#tokenization').tokenizationWidget(
      tokenization, {whitespaceTokenization: true});

  //});
  }*/

  render() {
    return (
      <div className="communication_container comm">
        <div id="tokenization"></div>
      </div>
    );
  }
  componentdidmount() {

  }

}
class EventTag extends React.Component {
  constructor() {
    super();
    this.change = this.change.bind(this);
    this.handleOrdinalChange = this.handleOrdinalChange.bind(this);
    this.state = {
        eventType: "No Event",
        ordinalRating: '3'
    };
  }


  change(event) {
    var value = event.target.value;
    //this.state.eventType = value;
    this.setState({eventType: value});
    console.log("EventType: " + this.state.eventType)
  }

  handleOrdinalChange(event) {
    var value = event.target.value;
    //this.state.ordinalRating = value
    this.setState({ordinalRating: value});
    console.log("ordinalRating:" + this.state.ordinalRating)
  }

  render() {
    return (
        <div className="event">
          <div className="btn-group inner" role="group">
            <select className="text_select btn btn-info" onChange={this.change} value={this.state.eventType} id="field_6" name="field_6">
              <option value="No Event">    No Event    </option>
              <option value="Civil Unrest or Wide-spread Crime">Civil Unrest or Wide-spread Crime</option>
              <option value="Elections and Politics">Elections and Politics</option>
              <option value="Evacuation">Evacuation</option>
              <option value="Food Supply">Food Supply</option>
              <option value="Infrastructure">Infrastructure</option>
              <option value="Medical Assistance">Medical Assistance</option>
              <option value="Search/Rescue">Search/Rescue</option>
              <option value="Shelter">Shelter</option>
              <option value="Terrorism or other Extreme Violence">Terrorism or other Extreme Violence</option>
              <option value="Utilities, Energy, or Sanitation">Utilities, Energy, or Sanitation</option>
              <option value="Water Supply">Water Supply</option>
            </select>
          </div>
          <div className="ordinal-group inner">
            <label><input type="radio" value='1' name="radioset" checked={this.state.ordinalRating === '1'}
                      onChange={this.handleOrdinalChange} />Possbile</label>
            <label><input type="radio" value='2' name="radioset" checked={this.state.ordinalRating === '2'}
                      onChange={this.handleOrdinalChange}/>Probable</label>
            <label><input type="radio" value="3" name="radioset" checked={this.state.ordinalRating === '3'}
                      onChange={this.handleOrdinalChange}/>Very Likely or Certain</label>
          </div>
        </div>
    );
  }
}

class AddEvent extends React.Component {
  constructor() {
    super();
    this.state = {
        numChildren: 0
    };
    this.addEvent = this.addEvent.bind(this);
  }

  addEvent() {
    if (this.state.numChildren < 12) {
      var event_tags = document.querySelectorAll('.content-events');//'#content-events-'+this.state.numChildren);
      var last_event = event_tags[0];
      var new_event = $(last_event).clone().attr('id', 'content-events-'+this.state.numChildren + 1);
      //new_event.appendTo(event_tags);
      new_event.insertBefore(document.querySelectorAll('#add-event')[0]);
      new_event.innerHTML= '';
      var currEventTag = ReactDOM.render(
        <EventTag />,
        //React.createElement(new EventTag, null),
        document.getElementById('content-events-'+this.state.numChildren + 1)
      );
      eventTags[this.state.numChildren+1] = currEventTag;

      /*var allEvents = document.querySelectorAll('.event');
      var lastEvent = allEvents[allEvents.length - 1];
      $(lastEvent).clone().appendTo(event_tags);*/
      //var clone = React.cloneElement(EventTag);
      this.setState({
        numChildren: this.state.numChildren + 1
      });
    }
    console.log(this.state.numChildren);
  }

  render() {
    return (
          <p className="btn btn-info add_event comm" onClick={this.addEvent}>
              <span className="glyphicon glyphicon-plus"></span>
          </p>
    );
  }
}

class SubmitButton extends React.Component {
  constructor() {
    super();
    this.submitSentence = this.submitSentence.bind(this);
    this.nextSentence = this.nextSentence.bind(this);
    this.state = {};
    /*maxSents: getMaxSents(),
    totalComms: COMMS.length,
    totalSentsLabeled: 0,
    currComm: 0,
    totalSentsInCurrComm: comm.sectionList[0].sentenceList.length,
    currSentInComm: 0,*/

    //console.log("Max Sents: " + this.state.maxSents);
    /*this.setState({
      totalSentsInCurrComm: COMMS[0].sectionList[0].sentenceList.length
    });*/
  }

  /*componentWillMount() {
    this.setState({maxSents: getMaxSents()});
    console.log("Max Sents: " + this.state.maxSents);
  }*/

  nextSentence() {
    // this is where I should add the data to the communication and then load the next sentence
    // store the data, increment counter, load the next one
    updateEventMapping(this.state.totalSentsLabeled);
    console.log("Sentence labeled Pre: " + this.state.totalSentsLabeled);

    this.setState({
      totalSentsLabeled: this.state.totalSentsLabeled + 1,
      currSentInComm: this.state.currSentInComm + 1
    });
    console.log("Sentence labeled Post: " + this.state.totalSentsLabeled);

    if (this.state.currSentInComm + 1 == this.state.totalSentsInCurrComm) {
      //move the communication to be annotated
      this.setState({
        currSentInComm: 0,
        totalSentsInCurrComm: COMMS[this.state.currComm + 1].sectionList[0].sentenceList.length,
        currComm: this.state.currComm + 1,
      });
      console.log("move to next communication")
    }

    console.log("Curr Comm: " + this.state.currComm);
    console.log("CurrSent: " + this.state.currSentInComm);
    console.log("total Sents in Curr Comm: " + this.state.totalSentsInCurrComm);
    updateDisplayedCommunications(COMMS[this.state.currComm], this.state.sentsLabeled, false);
    console.log("Sentence labeled: " + this.state.totalSentsLabeled);
    console.log("Max Sents: " + this.state.maxSents);
    addEventToComm(COMMS[this.state.currComm], this.state.currSentInComm)
  }

  submitSentence() {
    addEventToComm(COMMS[this.state.currComm], this.state.currSentInComm)
    //updateEventMapping(this.state.totalSentsLabeled);
    //submit the Communcation to the backend
    /*** submit the Communcation to the backend
       * Get the MTurk metadata from params for assignmentId, workerId, and hitId
       * Get Event Data from Event_MAP
     ***/

    /*var out = "";
    for(var i in EVENT_MAP) {
      out += i + " -> " + EVENT_MAP[i] + "\n";
    }*/
    alert("Submit the Communication to the backend\n" + out);
    try {
        if (COMMS && COMMS.length > 0) {
            for (var i = 0; i < COMMS.length; i++) {
                COMMS.results.submitAnnotation(
                    RESULTS_SERVER_SESSION_ID,
                    // The .annotationUnitIdentifier field is added by getNextCommunications()
                    COMMS[i].annotationUnitIdentifier,
                    COMMS[i]);
            }
        }
    }
    catch (error) {
    }
    //https://www.mturk.com/mturk/externalSubmit
    var AMAZON_HOST = "https://workersandbox.mturk.com/mturk/externalSubmit?"
    console.log("submitSentence hit");
    //change foo=bar to be list of (Commm, sentence, [(EventType, Liklihood Rating)])
    AMAZON_HOST += "assignmentId="+params["assignmentId"]+"&foo=bar";//+"&workerId="+params["workerId"]+"&hitId="+params["hitId"]
    for (var i in params) {
      //AMAZON_HOST += i + "=" + params[i] + "&";
      console.log(i +"="+params[i]);
    }
    console.log(AMAZON_HOST);
    $("#target").attr('action', AMAZON_HOST).attr('method', 'POST');
  }
    /*var urls = window.location.href.split(window.location.origin)
    fetch(window.location.origin+"/next?"+urls[1], {
      method: 'PUT',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        eventType: eventTag.state["eventType"],
        ordinalRating: eventTag.state["ordinalRating"],
      })
    }).then(function(responseObj) {
		  console.log('status: ', responseObj.status);
      //var fileNum = Math.floor(Math.random() * 100);
      //commContainer.props.fileNumber = fileNum  //make it the response from JSON
      //commContainer.setProps({ fileNumber: fileNum });
      //commContainer.change_text()
      eventTag.state.eventType = "Event 12";
      eventTag.state.ordinalRating = '1';
      eventTag.setState({eventType: "Event12", ordinalRating: '1'})

    });*/

  render() {
    if (this.state.totalSentsLabeled + 1 == this.state.maxSents) //+ 1 == this.state.maxSents)
    { console.log("Max Sents: " + this.state.maxSents);
      return (
        <form id="target">
          <div>
              <button class="btn btn-primary" id="submit_button" onClick={this.submitSentence}>Submit</button>
          </div>
        </form>

        //<button className="btn btn-default" type="submit" onClick={this.submitSentence}>Submit</button>-->
      )
    }
    return (
      <div>
          <button class="btn btn-primary" id="next_button" onClick={this.nextSentence}>Next</button>
      </div>
      //<button className="btn btn-default" type="submit" onClick={this.submitSentence}>Submit</button>-->
    )
  }
}
/*        <p className="btn btn-info add_event comm">
            <span className="glyphicon glyphicon-plus"></span>
        </p>
*/

// ========================================
//React.render(<App />, document.getElementById('root'));
var commContainer = ReactDOM.render(
  <CommunicationContainer />,
  document.getElementById('content-sentence')
);

var eventTags = [];
var eventTag = ReactDOM.render(
  <EventTag />,
  //React.createElement(new EventTag, null),
  document.getElementById('content-events-0')
);
eventTags[0] = eventTag;

ReactDOM.render(
  <AddEvent />,
  document.getElementById('add-event')
);

/*ReactDOM.render(
  <SubmitButton />,
  document.getElementById('submit-events')
);*/
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
    location.search.replace(/[?&]+([^=&]+)=([^&]*)/gi,function(s,k,v){params[k]=v})
    //the second part of the check is for debugging purposes
    if (params["assignmentId"] == "ASSIGNMENT_ID_NOT_AVAILABLE" || !("assignmentId" in params)) {
       //disable the next sentence button
       //console.log("TODO: disable the next sentence button");
     } else {
       submitButton = ReactDOM.render(
         <SubmitButton />,
         document.getElementById('submit-events')
       );
     }
     CADET.init();
     var resultsServerIdString = getUrlParameter("annotationSessionId");
     if (resultsServerIdString) {
       RESULTS_SERVER_SESSION_ID = new UUID();
       RESULTS_SERVER_SESSION_ID.uuidString = resultsServerIdString;

       console.log("RESULTS_SERVER_SESSION_ID: " +RESULTS_SERVER_SESSION_ID);
       try {
         var annotationUnitIdentifiers = CADET.results.getNextChunk(RESULTS_SERVER_SESSION_ID);
         for (var anno in annotationUnitIdentifiers) {
            console.log("COMM id: " + anno.communicationId + "; sentence: " + anno.sentenceId);
         }
         COMMS = getNextCommunications(annotationUnitIdentifiers);
       }
       catch (error) {
        console.log(error);
       }

     }

     /*var searchResultIdString = getUrlParameter('searchResultId');
     if (searchResultIdString) {
         var searchResultId = new UUID();
         searchResultId.uuidString = searchResultIdString;

         try {
             //RESULTS_SERVER_SESSION_ID = CADET.results.startSession(searchResultId);
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
     }*/

     updateDisplayedCommunications(COMMS[0], 0, true);
    //});
});
