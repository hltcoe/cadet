package edu.jhu.hlt.concrete.feedback;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;

import com.typesafe.config.Config;

import edu.jhu.hlt.cadet.CadetConfig;
import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.concrete.feedback.store.FeedbackStore;
import edu.jhu.hlt.concrete.feedback.store.SentenceFeedback;
import edu.jhu.hlt.concrete.feedback.store.SentenceIdentifier;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResults;

public class DumpFeedbackServlet extends HttpServlet {
    private static final long serialVersionUID = -3747969490436354514L;

    private String dumpDir;

    @Override
    public void init() {
        Config config = ConfigManager.getInstance().getConfig();
        dumpDir = config.getString(CadetConfig.FEEDBACK_DIR);
        if (dumpDir.charAt(dumpDir.length() - 1) != File.separatorChar) {
            dumpDir += File.separator;
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean error = false;
        String errorMessage = null;

        FeedbackHandler handler = ConfigManager.getInstance().getFeedbackHandler();
        FeedbackStore store = handler.getStore();
        Set<SentenceFeedback> feedbackSet = store.getAllSentenceFeedback();
        List<SearchResults> data = new ArrayList<SearchResults>();
        for (SentenceFeedback f : feedbackSet) {
            SearchResults results = f.getSearchResults();
            Map<SentenceIdentifier, SearchFeedback> feedback = f.getFeedback();
            for (SearchResult r : results.getSearchResults()) {
                SearchFeedback sf = feedback.get(new SentenceIdentifier(r.getCommunicationId(), r.getSentenceId()));
                if (sf != null) {
                    r.setScore(sf.getValue());
                } else {
                    r.setScore(0.0);
                }
            }
            data.add(results);
        }

        DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String filename = "feedback_" + timeStampPattern.format(java.time.LocalDateTime.now()) + ".tar.gz";
        try {
            toTarGz(data, dumpDir + filename);
        } catch (IOException e) {
            error = true;
            errorMessage = e.getMessage();
        }

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        if (!error) {
            out.println("Exported " + String.valueOf(feedbackSet.size()) + " search results");
        } else {
            out.println(errorMessage);
        }
        out.close();
    }

    private void toTarGz(Collection<SearchResults> results, String outFilename) throws IOException {        
        try (OutputStream os = Files.newOutputStream(Paths.get(outFilename));
             BufferedOutputStream bos = new BufferedOutputStream(os);
             GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos);) {

            for (SearchResults sr : results) {
                TarArchiveEntry entry = new TarArchiveEntry(sr.getUuid().getUuidString() + ".concrete");
                byte[] cbytes = this.toBytes(sr);
                entry.setSize(cbytes.length);
                tos.putArchiveEntry(entry);
                ByteArrayInputStream bis = new ByteArrayInputStream(cbytes);
                IOUtils.copy(bis, tos);
                tos.closeArchiveEntry();
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    private byte[] toBytes(SearchResults results) throws IOException {
        try {
            return new TSerializer(new TCompactProtocol.Factory()).serialize(results);
        } catch (TException e) {
            throw new IOException(e);
        }
    }

}
