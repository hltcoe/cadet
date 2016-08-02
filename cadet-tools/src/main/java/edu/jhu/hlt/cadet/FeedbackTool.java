package edu.jhu.hlt.cadet;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.serialization.BoundedThriftSerializer;
import edu.jhu.hlt.concrete.util.ConcreteException;

public class FeedbackTool {

    public static void main(String[] args) throws ConcreteException, IOException {
        if (args.length != 1) {
            System.err.println("Usage: ./feedback.sh <filename>");
            System.exit(-1);
        }

        BoundedThriftSerializer<SearchResults> deserializer = new BoundedThriftSerializer<>(SearchResults.class);
        Iterator<SearchResults> it = deserializer.fromTarGz(Paths.get(args[0]));
        while (it.hasNext()) {
            SearchResults sr = it.next();
            System.out.println(sr.getUuid().getUuidString());
            System.out.println("--------------------------------------------");
            for (SearchResult r : sr.getSearchResults()) {
                System.out.print(r.getCommunicationId());
                System.out.print("\t");
                System.out.print(r.getSentenceId().getUuidString());
                System.out.print("\t");
                System.out.print(r.getScore());
                System.out.print("\t");
                System.out.print(sr.getSearchQuery().getRawQuery());
                System.out.println();
            }
            System.out.println();
        }

    }

}
