package com.ovs.controllers;

import com.ovs.dao.VoteDAO;
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
import java.sql.SQLException;

/**
 * Controller handling ballot submission by authenticated voters.
 * Orchestrates atomic vote casting via {@link VoteDAO#castVote(long, long, long)},
 * enforces secret ballot privacy, stores digital receipt tokens in session,
 * and routes to the vote confirmation view.
 */
@WebServlet("/voter/cast-vote")
public class VoteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private VoteDAO voteDAO;

    @Override
    public void init() throws ServletException {
        this.voteDAO = new VoteDAO();
    }

    /**
     * Rejects direct GET attempts to cast ballots and redirects to the voter dashboard.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/voter/dashboard");
    }

    /**
     * Processes POST submission of a voter's ballot choice.
     * Validates active session context, parses target election and candidate IDs,
     * commits the transaction atomically, and preserves the confirmation receipt.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // 1. Retrieve authenticated voter from session
        HttpSession session = request.getSession(false);
        Voter voter = (session != null) ? (Voter) session.getAttribute("currentUser") : null;

        if (voter == null) {
            String errMsg = URLEncoder.encode("You must be logged in to cast a vote.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
            return;
        }

        // Enforce mandatory voter profile completion gatekeeper
        if (!voter.isProfileComplete()) {
            String warningMsg = URLEncoder.encode("Please complete your official student profile before participating in any elections.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/voter/profile?warning=" + warningMsg);
            return;
        }

        // 2. Validate CSRF Protection Token
        if (!com.ovs.util.CsrfUtil.isValidToken(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Security Validation Failed: Invalid or missing CSRF token. Potential cross-site request forgery detected.");
            return;
        }

        // 3. Extract and parse parameters
        String electionIdParam = request.getParameter("electionId");
        String candidateIdParam = request.getParameter("candidateId");

        if (electionIdParam == null || electionIdParam.trim().isEmpty() ||
            candidateIdParam == null || candidateIdParam.trim().isEmpty()) {

            String errMsg = URLEncoder.encode("Invalid ballot submission: Election or Candidate was not specified.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/voter/dashboard?error=" + errMsg);
            return;
        }

        long electionId;
        long candidateId;
        try {
            electionId = Long.parseLong(electionIdParam.trim());
            candidateId = Long.parseLong(candidateIdParam.trim());
        } catch (NumberFormatException e) {
            String errMsg = URLEncoder.encode("Invalid election or candidate identifier format.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/voter/dashboard?error=" + errMsg);
            return;
        }

        // 3. Execute atomic, ACID-compliant vote recording transaction
        try {
            String receiptToken = voteDAO.castVote(voter.getVoterId(), electionId, candidateId);

            // Update voter status in current session memory
            voter.setHasVoted(true);
            session.setAttribute("currentUser", voter);

            // Store receipt token and election metadata in session for confirmation receipt display
            session.setAttribute("receiptToken", receiptToken);
            session.setAttribute("confirmedElectionId", electionId);
            session.setAttribute("confirmedCandidateId", candidateId);

            // Also set as request attributes in case of same-request forwarding
            request.setAttribute("receiptToken", receiptToken);

            // Redirect following Post/Redirect/Get (PRG) pattern to prevent duplicate form submissions
            response.sendRedirect(request.getContextPath() + "/voter/vote-confirmation.jsp");

        } catch (Exception e) {
            System.err.println("Voting failed for voter " + voter.getVoterId() + ": " + e.getMessage());
            e.printStackTrace();

            String userMsg;
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate vote")) {
                userMsg = URLEncoder.encode("Duplicate vote blocked: You have already cast your ballot in this election.", StandardCharsets.UTF_8);
            } else {
                userMsg = URLEncoder.encode("An error occurred while submitting your ballot. Please try again.", StandardCharsets.UTF_8);
            }
            response.sendRedirect(request.getContextPath() + "/voter/dashboard?error=" + userMsg);
        }
    }
}
