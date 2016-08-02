package edu.jhu.hlt.cadet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.jhu.hlt.concrete.TextSpan;
import edu.jhu.hlt.concrete.TokenRefSequence;
import edu.jhu.hlt.concrete.search.Search;
import edu.jhu.hlt.concrete.search.SearchQuery;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;
import edu.jhu.hlt.concrete.search.SearchType;

public class SearchTool implements AutoCloseable {
    private String host;
    private int port;
    private String auths;

    private TTransport transport;
    private TCompactProtocol protocol;
    private Search.Client client;

    public SearchTool() {
        Config config = ConfigFactory.load();
        host = config.getString("search.host");
        port = config.getInt("search.port");
        auths = config.getString("accumulo.auths");

        transport = new TFramedTransport(new TSocket(host, port), Integer.MAX_VALUE);
        protocol = new TCompactProtocol(transport);
        client = new Search.Client(protocol);
    }

    public SearchResults search(String query, String type) {

        SearchQuery searchQuery = prepareQuery(query, type);

        SearchResults results = null;
        try {
            transport.open();
            results = client.search(searchQuery);
            transport.close();
        } catch (TException e) {
            e.printStackTrace();
        }

        return results;
    }

    protected SearchQuery prepareQuery(String query, String type) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setType(type.equals("comm") ? SearchType.COMMUNICATIONS : SearchType.SENTENCES);
        searchQuery.setRawQuery(query);
        searchQuery.setAuths(auths);
        Output output = getQuestions(query);
        searchQuery.setQuestions(output.items);
        searchQuery.setTerms(getTerms(output.query));

        // TODO: user id, query name, labels, communication id, tokens

        return searchQuery;
    }

    // copied from search UI JavaScript
    private static final String WORD_WITH_SPACE = "[^\\?\\\"]";
    private static final String WORD = "[^\\?\\\"\\s]";

    protected Output getQuestions(String query) {
        // ?:What is blue?
        // ?:"What is blue?"
        // ?:What is blue
        Pattern pattern = Pattern.compile("\\?\\:(\\\")?(" + WORD + "+\\s*)+(\\?)?(\\\")?");
        Matcher matcher = pattern.matcher(query);
        List<String> questions = new ArrayList<String>();
        while (matcher.find()) {
            String group = matcher.group();
            query = query.replaceFirst(Pattern.quote(group), "");
            group = group.replaceAll("^\\?", "").replaceAll("[:\\\"]", "");
            questions.add(group);
        }
        query = query.trim();

        return new Output(query, questions);
    }

    protected List<String> getTerms(String query) {
        // "queen elizabeth"
        // groceries expensive foods
        Pattern pattern = Pattern.compile("\\\"" + WORD_WITH_SPACE + "+\\\"|" + WORD + "+");
        Matcher matcher = pattern.matcher(query);
        List<String> terms = new ArrayList<String>();
        while (matcher.find()) {
            String group = matcher.group();
            group = group.replaceAll("\"", "");
            terms.add(group);
        }

        return terms;
    }

    protected static class Output {
        public String query;
        public List<String> items;

        public Output(String query, List<String> items) {
            this.query = query;
            this.items = items;
        }
    }

    @Override
    public void close() {
        if (this.transport.isOpen())
            this.transport.close();
    }

    static class Opts {
        @Parameter(description = "Search query", required = true)
        List<String> queryList;

        @Parameter(names = {"--type", "-t"}, description = "Query type (comm, sent)")
        String type = "sent";

        @Parameter(help = true, names = {"--help", "-h"}, description = "Print the help message and exit.")
        boolean help;
    }

    public static void main(String[] args) {
        Opts opts = new Opts();
        JCommander jc = new JCommander(opts);
        jc.setProgramName("./search.sh");
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            jc.usage();
            System.exit(-1);
        }
        if (opts.help) {
            jc.usage();
            return;
        }

        String query = String.join(" ", opts.queryList);
        try (SearchTool tool = new SearchTool();) {
            SearchResults results = tool.search(query, opts.type);
            if (results == null) {
                System.err.println("Failed to get search result");
                System.exit(-1);
            }

            System.out.println("Score\tCommunicationId\tSentenceId\tTokens");
            for (SearchResult result : results.getSearchResults()) {
                System.out.print(result.getScore());
                System.out.print("\t");
                System.out.print(result.getCommunicationId());
                System.out.print("\t");
                System.out.print(result.getSentenceId());
                System.out.print("\t");
                if (result.isSetTokens()) {
                    TokenRefSequence tokens = result.getTokens();
                    TextSpan span = tokens.getTextSpan();
                    if (span != null) {
                        System.out.print(span.getStart());
                        System.out.print("-");
                        System.out.print(span.getEnding());
                    } else {
                        System.out.print("no text span");
                    }
                } else {
                    System.out.print("null");
                }
                System.out.println();
            }
        }
    }
}
