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
import java.util.List;

/**
 * Controller serving the secure voting booth view for a chosen election.
 * Validates voter participation eligibility and populates candidate profiles
 * before dispatching to the ballot booth interface.
 */
@WebServlet("/voter/vote")
public class VoteBoothServlet extends HttpServlet {

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
            String errMsg = URLEncoder.encode("Please log in to cast your ballot.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
            return;
        }

        // Enforce mandatory voter profile completion gatekeeper
        if (!voter.isProfileComplete()) {
            String warningMsg = URLEncoder.encode("Please complete your official student profile before participating in any elections.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/voter/profile?warning=" + warningMsg);
            return;
        }

        String electionIdParam = request.getParameter("electionId");
        if (electionIdParam == null || electionIdParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/voter/dashboard");
            return;
        }

        long electionId;
        try {
            electionId = Long.parseLong(electionIdParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/voter/dashboard?error=invalid_election");
            return;
        }

        // Verify that voter has not already voted in this election
        boolean alreadyVoted = false;
        try {
            alreadyVoted = voteDAO.hasVoterVotedInElection(voter.getVoterId(), electionId);
        } catch (Exception e) {
            System.err.println("Error checking if voter voted: " + e.getMessage());
        }

        if (alreadyVoted) {
            String msg = URLEncoder.encode("You have already cast your ballot in this election.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/voter/dashboard?error=" + msg);
            return;
        }

        // Fetch election and associated candidate choices
        Election election = null;
        try {
            election = electionDAO.getElectionById(electionId);
        } catch (Exception e) {
            System.err.println("Error fetching election (" + electionId + "): " + e.getMessage());
        }

        if (election == null) {
            response.sendRedirect(request.getContextPath() + "/voter/dashboard?error=election_not_found");
            return;
        }

        List<Candidate> candidates = new java.util.ArrayList<>();
        try {
            candidates = candidateDAO.getCandidatesByElection(electionId);
        } catch (Exception e) {
            System.err.println("Error fetching candidates for election (" + electionId + "): " + e.getMessage());
        }
        if (candidates == null) {
            candidates = new java.util.ArrayList<>();
        }

        request.setAttribute("election", election);
        request.setAttribute("candidates", candidates);

        request.getRequestDispatcher("/voter/vote.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
