package com.ovs.controllers;

import com.ovs.dao.CandidateDAO;
import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoteDAO;
import com.ovs.models.Candidate;
import com.ovs.models.Election;
import com.ovs.models.Voter;
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
 * Controller serving the voter's main dashboard.
 * Queries active elections, competing candidates, and voter participation statuses,
 * attaching all necessary attributes before forwarding to the voter dashboard view.
 */
@WebServlet("/voter/dashboard")
public class VoterDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ElectionDAO electionDAO;
    private CandidateDAO candidateDAO;
    private VoteDAO voteDAO;

    @Override
    public void init() throws ServletException {
        this.electionDAO = new ElectionDAO();
        this.candidateDAO = new CandidateDAO();
        this.voteDAO = new VoteDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Voter voter = (session != null) ? (Voter) session.getAttribute("currentUser") : null;

        if (voter == null) {
            String errMsg = URLEncoder.encode("Please log in to access the voter dashboard.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
            return;
        }

        // Fetch all active elections
        List<Election> activeElections = electionDAO.getActiveElections();

        // Map candidates and voting status for each election
        Map<Long, List<Candidate>> candidatesMap = new HashMap<>();
        Map<Long, Boolean> votingStatusMap = new HashMap<>();

        for (Election election : activeElections) {
            long electionId = election.getElectionId();
            List<Candidate> candidates = candidateDAO.getCandidatesByElection(electionId);
            candidatesMap.put(electionId, candidates);

            boolean hasVoted = voteDAO.hasVoterVotedInElection(voter.getVoterId(), electionId);
            votingStatusMap.put(electionId, hasVoted);
        }

        request.setAttribute("activeElections", activeElections);
        request.setAttribute("candidatesMap", candidatesMap);
        request.setAttribute("votingStatusMap", votingStatusMap);

        request.getRequestDispatcher("/voter/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
