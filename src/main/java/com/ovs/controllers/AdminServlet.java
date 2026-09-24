package com.ovs.controllers;

import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoteDAO;
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
 * Controller serving the administrative dashboard.
 * Verifies election administrator credentials, aggregates live election data
 * and tabulated candidate vote counts via {@link ElectionDAO} and {@link VoteDAO},
 * and forwards the model to the administrative view.
 */
@WebServlet("/admin/dashboard")
public class AdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ElectionDAO electionDAO;
    private VoteDAO voteDAO;

    @Override
    public void init() throws ServletException {
        this.electionDAO = new ElectionDAO();
        this.voteDAO = new VoteDAO();
    }

    /**
     * Handles GET requests to load the administrative overview.
     * Verifies admin session, fetches active and recorded elections,
     * tallies live candidate results, and forwards to /admin/dashboard.jsp.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verify administrator session
        HttpSession session = request.getSession(false);
        Admin admin = (session != null) ? (Admin) session.getAttribute("currentAdmin") : null;

        if (admin == null) {
            String errMsg = URLEncoder.encode("Administrator authentication required.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/login.jsp?error=" + errMsg);
            return;
        }

        // 2. Fetch elections
        List<Election> activeElections = electionDAO.getActiveElections();
        List<Election> allElections = electionDAO.getAllElections();

        // 3. Aggregate live vote tallies for all elections
        Map<Long, List<CandidateResult>> electionResultsMap = new HashMap<>();
        for (Election election : allElections) {
            List<CandidateResult> results = voteDAO.getElectionResults(election.getElectionId());
            electionResultsMap.put(election.getElectionId(), results);
        }

        // 4. Attach model attributes to request scope
        request.setAttribute("currentAdmin", admin);
        request.setAttribute("activeElections", activeElections);
        request.setAttribute("allElections", allElections);
        request.setAttribute("electionResultsMap", electionResultsMap);

        // 5. Forward to admin dashboard JSP view
        request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
    }

    /**
     * Handles POST requests by delegating to doGet.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
