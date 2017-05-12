
import argparse
from concrete.access import FetchCommunicationService
from concrete.util.thrift_factory import factory
from concrete.util import generate_UUID
from concrete.search.ttypes import SearchQuery, SearchResult, SearchResultItem, SearchType
from concrete.services.results import ResultsServerService
from concrete.services.ttypes import AnnotationTaskType
from thrift.protocol import TJSONProtocol
from thrift.transport import THttpClient
import pdb

def main():

    # get list of comms from fetch service
    socket = factory.createSocket("localhost", 9090)
    transport = factory.createTransport(socket)
    protocol = factory.createProtocol(transport)
    fetch_client = FetchCommunicationService.Client(protocol)
    transport.open()

    comms = []
    offset = 0
    batch_size = 100
    '''
    while True:
        batch = fetch_client.getCommunicationIDs(offset, batch_size)
        pdb.set_trace()
        if len(batch) > 0:
            comms.extend(batch)
            offset += len(batch)
        else:
            break
    '''
    print("Number of communications: {}".format(len(comms)))

    transport.close()


    # create a fake search result object
    query = SearchQuery()
    query.type = SearchType.SENTENCES
    query.rawQuery = "Fake search"

    items = []
    comms_sents_f = open("non_null_tuples.tsv", "rb")
    for line in comms_sents_f: #comm_id in comms:
        # TODO no sentence ID - only way to get that is retrieve from fetch service
        comm_id = line.split('\t')[0]
        sentence_id = generate_UUID()
        sentence_id.uuidString = line.split('\t')[1].split('\n')[0]
        item = SearchResultItem(comm_id, sentenceId=sentence_id, score=0.5)
        items.append(item)

    search_result = SearchResult()
    search_result.uuid = generate_UUID()
    search_result.searchQuery = query
    search_result.searchResultItems = items#[:5]
    search_result.lang = "ara"


    # register the search result with the broker
    transport = THttpClient.THttpClient("localhost", 8080, "/CadetSearch/ResultsServer")
    protocol = TJSONProtocol.TJSONProtocol(transport)
    broker_client = ResultsServerService.Client(protocol)
    transport.open()

    broker_client.registerSearchResult(search_result, AnnotationTaskType.NER)
    session_id = broker_client.startSession(search_result.uuid, AnnotationTaskType.NER)
    print(session_id)

    transport.close()

if __name__ == "__main__":
    main()
