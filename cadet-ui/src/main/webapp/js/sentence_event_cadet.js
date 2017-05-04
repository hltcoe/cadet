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

// Global variables
var COMMS = [];
var RESULTS_SERVER_SESSION_ID = null;
var EVENT_MAP = {};
var params={};

var funcs = [];

var events = {}

function updateEventMapping(key) {
  EVENT_MAP[key] = eventTag.state.eventType + ":::" + eventTag.state.ordinalRating;
}

function updateDisplayedCommunications(comms, sentNum, first) {
  //change_text(comms[0])
  var comm = comms[0];
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

  if (!first) {
    document.getElementById("tokenization").innerHTML = '';
  }

  var tokenization = comm.sectionList[sentNum].sentenceList[0].tokenization;//.tokenList;//tokenizationList[0];//[1];
  var tokenizationWidget = $('#tokenization').tokenizationWidget(
    tokenization, {whitespaceTokenization: true});
}

class CommunicationContainer extends React.Component {
  constructor() {
    super();
    this.props = {
        fileNumber: 1
    };
    //this.change_text = this.change_text.bind(this);

  }

  change_text(i) {
    //$.getJSON('uz_data/uz_'+this.props.fileNumber+'_.concrete.json', function(commJSONData) {

      //var comm = new Communication();
      //comm.initFromTJSONProtocolObject(commJSONData);

    var tokenization = tokenizationList[i];//[1];
    document.getElementById("tokenization").innerHTML = '';

    var tokenizationWidget = $('#tokenization').tokenizationWidget(
      tokenization, {whitespaceTokenization: true});

  //});
  }

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
        eventType: "Event 12",
        ordinalRating: '1'
    };
  }


  change(event) {
    var value = event.target.value;
    this.state.eventType = value;
    this.setState({eventType: value});
    console.log("EventType: " + this.state.eventType)
  }

  handleOrdinalChange(event) {
    var value = event.target.value;
    this.state.ordinalRating = value
    this.setState({ordinalRating: value});
    console.log("ordinalRating:" + this.state.ordinalRating)
  }

  render() {
    return (
        <div className="event">
          <div className="btn-group inner" role="group">
            <select className="text_select btn btn-info" onChange={this.change} value={this.state.eventType} id="field_6" name="field_6">
              <option value="Event 12">no event</option>
              <option value="Event 1">med</option>
              <option value="Event 2">shelter</option>
              <option value="Event 3">food</option>
              <option value="Event 4">search</option>
              <option value="Event 5">terrorism</option>
              <option value="Event 6">crime_violence</option>
              <option value="Event 7">infra</option>
              <option value="Event 8">utils</option>
              <option value="Event 9">evac</option>
              <option value="Event 10">water</option>
              <option value="Event 11">regime_change</option>
            </select>
          </div>
          <div className="ordinal-group inner">
            <label><input type="radio" value='1' name="radioset" checked={this.state.ordinalRating === '1'}
                      onChange={this.handleOrdinalChange} />Very Unlikely</label>
            <label><input type="radio" value='2' name="radioset" checked={this.state.ordinalRating === '2'}
                      onChange={this.handleOrdinalChange}/>Unlikely</label>
            <label><input type="radio" value="3" name="radioset" checked={this.state.ordinalRating === '3'}
                      onChange={this.handleOrdinalChange}/>Possible</label>
            <label><input type="radio" value="4" name="radioset" checked={this.state.ordinalRating === '4'}
                      onChange={this.handleOrdinalChange}/>Likely</label>
            <label><input type="radio" value="5" name="radioset" checked={this.state.ordinalRating === '5'}
                      onChange={this.handleOrdinalChange}/>Very Likely</label>
          </div>
        </div>
    );
  }
}

class AddEvent extends React.Component {
  constructor() {
    super();
    this.state = {
        numChildren: 1
    };
    this.addEvent = this.addEvent.bind(this);
  }

  addEvent() {
    if (this.state.numChildren < 13) {
      var event_tags = document.querySelectorAll('#content-events');
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
    this.state = {
      sentsLabeled: 0
    }
  }

  nextSentence() {
    // this is where I should add the data to the communication and then load the next sentence
    // store the data, increment counter, load the next one
    //getNextCommunications(annotationUnitIdentifiers);
    updateEventMapping(this.state.sentsLabeled);
    updateDisplayedCommunications(COMMS, this.state.sentsLabeled, false);
    this.setState({
      sentsLabeled: this.state.sentsLabeled + 1
    });
    console.log("Sentence labeled: " + this.state.sentsLabeled);

  }

  submitSentence() {
    updateEventMapping(this.state.sentsLabeled);
    //submit the Communcation to the backend
    var out = "";
    for(var i in EVENT_MAP) {
      out += i + " -> " + EVENT_MAP[i] + "\n";
    }
    alert("Submit the Communication to the backend\n" + out);
    //https://www.mturk.com/mturk/externalSubmit
    var AMAZON_HOST = "https://workersandbox.mturk.com/mturk/externalSubmit?"
    console.log("submitSentence hit");
    AMAZON_HOST += "assignmentId="+params["assignmentId"]+"&foo=bar";//+"&workerId="+params["workerId"]+"&hitId="+params["hitId"]
    for (var i in params) {
      //AMAZON_HOST += i + "=" + params[i] + "&";
      console.log(i +"="+params[i]);
    }
    console.log(AMAZON_HOST);
    $("#target").attr('action', AMAZON_HOST).attr('method', 'POST');
    /*try {
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
        COMMS = getNextCommunications(annotationUnitIdentifiers);
        if (COMMS.length > 0) {
            updateDisplayedCommunications(COMMS, false);
        }
        else {
            location.replace("results.html");
        }
    }
    catch (error) {
    }*/

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
    if (this.state.sentsLabeled == 4)
    {
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

var eventTag = ReactDOM.render(
  <EventTag />,
  //React.createElement(new EventTag, null),
  document.getElementById('content-events')
);

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
       console.log("TODO: disable the next sentence button");
     } else {
       ReactDOM.render(
         <SubmitButton />,
         document.getElementById('submit-events')
       );
     }
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

     updateDisplayedCommunications(COMMS, 0, true);
    //});
});
