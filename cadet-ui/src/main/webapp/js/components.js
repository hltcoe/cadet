/*class EventSet extends React.Component {
  constructor () {
      this.state = {
          numChildren: 1
      };
  }

  render () {
      const children = [];

      for (var i = 0; i < this.state.numChildren; i += 1) {
          children.push(<EventTag number={i} />);
      };

      return (
          <ParentComponent addChild={this.onAddChild.bind(this)}>
              {children}
          </ParentComponent>
      );
  }

  onAddChild () {
      this.setState({
          numChildren: this.state.numChildren + 1
      });
  }
}*/

var events = {}

class CommunicationContainer extends React.Component {
  constructor() {
    super();
    this.props = {
        fileNumber: 1
    };
    this.change_text = this.change_text.bind(this);

  }

  change_text() {
    $.getJSON('uz_data/uz_'+this.props.fileNumber+'_.concrete.json', function(commJSONData) {

      var comm = new Communication();
      comm.initFromTJSONProtocolObject(commJSONData);

      var tokenizationList = comm.getTokenizationsAsList();
      while (tokenizationList.length < 500) {
        var tok = Token;
        tok.text = "\t ";
        tokenizationList.push(tok);
      }
      var tokenization = tokenizationList[4];//[1];
      document.getElementById("tokenization").innerHTML = ''

      var tokenizationWidget = $('#tokenization').tokenizationWidget(
        tokenization, {whitespaceTokenization: true});

    });
  }

  render() {
    return (
      <div className="communication_container comm">
        <div id="tokenization"></div>
      </div>
    );
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
  }

  submitSentence() {
    //var url = window.location.href;
    var urls = window.location.href.split(window.location.origin)
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
      var fileNum = Math.floor(Math.random() * 100);
      commContainer.props.fileNumber = fileNum  //make it the response from JSON
      //commContainer.setProps({ fileNumber: fileNum });
      //commContainer.change_text()
      eventTag.state.eventType = "Event 12";
      eventTag.state.ordinalRating = '1';
      eventTag.setState({eventType: "Event12", ordinalRating: '1'})

    });

  }

  render() {
    return (
      <button className="btn btn-default" type="submit" onClick={this.submitSentence}>Submit</button>
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
