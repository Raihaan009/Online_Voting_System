package com.ovs.controllers.admin;

import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoteDAO;
import com.ovs.models.Election;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller handling administrative live tabulation and cryptographic audit trails:
 * Computes live vote counts, turnout percentages, and allows auditing of SHA-256 digital receipts.
 */
@WebServlet("/admin/results")
public class AdminResultsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ElectionDAO electionDAO;
    private VoteDAO voteDAO;

    @Override
    public void init() throws ServletException {
        this.electionDAO = new ElectionDAO();
        this.voteDAO = new VoteDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<Election> allElections = electionDAO.getAllElections();
        request.setAttribute("allElections", allElections);

        String electionIdParam = request.getParameter("electionId");
        long selectedElectionId = -1;

        if (electionIdParam != null && !electionIdParam.trim().isEmpty()) {
            try {
                selectedElectionId = Long.parseLong(electionIdParam.trim());
            } catch (NumberFormatException ignored) {}
        } else if (!allElections.isEmpty()) {
            selectedElectionId = allElections.get(0).getElectionId();
        }

        if (selectedElectionId > 0) {
            Election selectedElection = electionDAO.getElectionById(selectedElectionId);
            Map<String, Object> analytics = voteDAO.getElectionAnalytics(selectedElectionId);

            request.setAttribute("selectedElectionId", selectedElectionId);
            request.setAttribute("selectedElection", selectedElection);
            request.setAttribute("analytics", analytics);
        }

        // Handle Cryptographic Receipt Audit Search
        String receiptToken = request.getParameter("receiptToken");
        if (receiptToken != null && !receiptToken.trim().isEmpty()) {
            receiptToken = receiptToken.trim();
            boolean isValid = voteDAO.verifyReceiptToken(selectedElectionId, receiptToken);
            Map<String, Object> auditDetails = voteDAO.getReceiptAuditDetails(receiptToken);

            request.setAttribute("auditTokenSearched", receiptToken);
            request.setAttribute("auditTokenValid", isValid);
            request.setAttribute("auditDetails", auditDetails);
        }

        request.getRequestDispatcher("/admin/results.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
