package com.ovs.controllers.admin;

import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoteDAO;
import com.ovs.dao.VoterDAO;
import com.ovs.models.Admin;
import com.ovs.models.CandidateResult;
import com.ovs.models.Election;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller serving the primary administrative control center.
 * Aggregates high-level electoral health metrics, active election windows,
 * live ballot counts, and candidate standings.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ElectionDAO electionDAO;
    private VoteDAO voteDAO;
    private VoterDAO voterDAO;

    @Override
    public void init() throws ServletException {
        this.electionDAO = new ElectionDAO();
        this.voteDAO = new VoteDAO();
        this.voterDAO = new VoterDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Admin admin = (session != null) ? (Admin) session.getAttribute("currentAdmin") : null;

        if (admin == null) {
            String errMsg = URLEncoder.encode("Administrator authentication required.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/login.jsp?error=" + errMsg);
            return;
        }

        // 1. Fetch elections data
        List<Election> allElections = electionDAO.getAllElections();
        List<Election> activeElections = electionDAO.getActiveElections();

        // 2. Fetch voter and ballot tallies
        long totalBallotsCast = voteDAO.getTotalBallotsCastCount();
        int totalRegisteredVoters = voterDAO.getAllVoters().size();

        // 3. Tabulate live results and analytics maps per election
        Map<Long, List<CandidateResult>> electionResultsMap = new HashMap<>();
        Map<Long, Map<String, Object>> electionAnalyticsMap = new HashMap<>();

        for (Election election : allElections) {
            long electionId = election.getElectionId();
            electionResultsMap.put(electionId, voteDAO.getElectionResults(electionId));
            electionAnalyticsMap.put(electionId, voteDAO.getElectionAnalytics(electionId));
        }

        // 4. Attach attributes to request scope
        request.setAttribute("currentAdmin", admin);
        request.setAttribute("allElections", allElections);
        request.setAttribute("activeElections", activeElections);
        request.setAttribute("totalBallotsCast", totalBallotsCast);
        request.setAttribute("totalRegisteredVoters", totalRegisteredVoters);
        request.setAttribute("electionResultsMap", electionResultsMap);
        request.setAttribute("electionAnalyticsMap", electionAnalyticsMap);

        // 5. Forward to admin dashboard view
        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
