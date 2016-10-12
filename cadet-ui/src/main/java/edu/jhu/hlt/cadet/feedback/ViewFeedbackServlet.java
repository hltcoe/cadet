package edu.jhu.hlt.cadet.feedback;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.jhu.hlt.cadet.ConfigManager;
import edu.jhu.hlt.cadet.feedback.store.CommunicationFeedback;
import edu.jhu.hlt.cadet.feedback.store.FeedbackStore;
import edu.jhu.hlt.cadet.feedback.store.SentenceFeedback;
import edu.jhu.hlt.cadet.feedback.store.SentenceIdentifier;
import edu.jhu.hlt.concrete.search.SearchFeedback;
import edu.jhu.hlt.concrete.search.SearchResult;
import edu.jhu.hlt.concrete.search.SearchResultItem;

/**
 * View the feedback currently in the store
 */
public class ViewFeedbackServlet extends HttpServlet {
    private static final long serialVersionUID = 283945342041275841L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        FeedbackHandler handler = ConfigManager.getInstance().getFeedbackHandler();
        FeedbackStore store = handler.getStore();
        Set<SentenceFeedback> feedbackSet = store.getAllSentenceFeedback();
        for (SentenceFeedback f : feedbackSet) {
            out.println(f.getSearchResults().getUuid().getUuidString());
            out.println("----------------------------------------------------");
            SearchResult results = f.getSearchResults();
            Map<SentenceIdentifier, SearchFeedback> feedback = f.getFeedback();
            for (SearchResultItem r : results.getSearchResultItems()) {
                out.print(r.getCommunicationId());
                out.print("\t");
                out.print(r.getSentenceId().getUuidString());
                out.print("\t");
                SearchFeedback sf = feedback.get(new SentenceIdentifier(r.getCommunicationId(), r.getSentenceId()));
                if (sf != null) {
                    out.print(sf.getValue());
                } else {
                    out.print(0);
                }
                out.println();
            }
            out.println();
        }

        Set<CommunicationFeedback> commFeedbackSet = store.getAllCommunicationFeedback();
        for (CommunicationFeedback f : commFeedbackSet) {
            out.println(f.getSearchResults().getUuid().getUuidString());
            out.println("----------------------------------------------------");
            SearchResult results = f.getSearchResults();
            Map<String, SearchFeedback> feedback = f.getFeedback();
            for (SearchResultItem r : results.getSearchResultItems()) {
                out.print(r.getCommunicationId());
                out.print("\t");
                SearchFeedback sf = feedback.get(r.getCommunicationId());
                if (sf != null) {
                    out.print(sf.getValue());
                } else {
                    out.print(0);
                }
                out.println();
            }
        }

        if (feedbackSet.isEmpty() && commFeedbackSet.isEmpty()) {
            out.println("No feedback");
        }

        out.close();
    }
}
