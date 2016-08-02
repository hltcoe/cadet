package edu.jhu.hlt.concrete.results;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalTime;

import edu.jhu.hlt.cadet.ConfigManager;

public class ViewSessionsServlet extends HttpServlet {
    private static final long serialVersionUID = 1587075632693678963L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        ResultsHandler handler = ConfigManager.getInstance().getResultsHandler();
        List<AnnotationSession> sessions = handler.getActiveSessions();
        if (sessions.isEmpty()) {
            out.println("No active sessions at " + LocalTime.now().toString());
        } else {
            for (AnnotationSession session : sessions) {
                out.println(session.getId().getUuidString());
            }
        }

        out.close();
    }
}
