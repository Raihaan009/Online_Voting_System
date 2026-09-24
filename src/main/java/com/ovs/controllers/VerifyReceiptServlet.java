package com.ovs.controllers;

import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoteDAO;
import com.ovs.models.Election;
import com.ovs.models.Vote;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Public Independent Ballot Verification Controller.
 * Enables voters and third-party auditors to independently verify that a ballot
 * was legitimately committed to the immutable database ledger using its SHA-256 receipt token.
 *
 * CRITICAL PRIVACY ASSURANCE:
 * In accordance with secret ballot principles, this endpoint intentionally omits
 * the voter's identity and specific candidate choice, proving inclusion without coercion risk.
 */
@WebServlet("/verify-receipt")
public class VerifyReceiptServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private VoteDAO voteDAO;
    private ElectionDAO electionDAO;

    @Override
    public void init() throws ServletException {
        this.voteDAO = new VoteDAO();
        this.electionDAO = new ElectionDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processVerification(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processVerification(request, response);
    }

    private void processVerification(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String receiptToken = request.getParameter("receiptToken");

        if (receiptToken != null && !receiptToken.trim().isEmpty()) {
            String token = receiptToken.trim();
            request.setAttribute("searched", true);
            request.setAttribute("receiptToken", token);

            // Validate format: SHA-256 token must be 64 hexadecimal characters
            if (!token.matches("^[a-fA-F0-9]{64}$")) {
                request.setAttribute("verified", false);
                request.setAttribute("errorMessage", 
                        "Invalid receipt format. A valid SHA-256 digital receipt must be exactly 64 hexadecimal characters.");
            } else {
                Vote vote = voteDAO.getVoteByReceiptToken(token);

                if (vote != null) {
                    Election election = electionDAO.getElectionById(vote.getElectionId());
                    String electionTitle = (election != null) ? election.getTitle() : "Election #" + vote.getElectionId();
                    String electionStatus = (election != null) ? election.getStatus() : "CONFIRMED";

                    request.setAttribute("verified", true);
                    request.setAttribute("electionTitle", electionTitle);
                    request.setAttribute("electionStatus", electionStatus);
                    request.setAttribute("voteTimestamp", vote.getVoteTimestamp());
                    request.setAttribute("ballotId", vote.getVoteId());
                } else {
                    request.setAttribute("verified", false);
                    request.setAttribute("errorMessage", 
                            "No recorded ballot matching this receipt token was found in the secret ballot box.");
                }
            }
        } else {
            request.setAttribute("searched", false);
        }

        request.getRequestDispatcher("/verify.jsp").forward(request, response);
    }
}
