package com.ovs.controllers.admin;

import com.ovs.dao.ElectionDAO;
import com.ovs.dao.VoteDAO;
import com.ovs.models.Election;
import com.ovs.models.Voter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller handling administrative live tabulation and cryptographic audit trails:
 * Computes live vote counts, turnout percentages, on-demand candidate voter breakdown audits,
 * and allows auditing of SHA-256 digital receipts.
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

        // Check for On-Demand Candidate Voter Breakdown Audit (JSON response)
        String action = request.getParameter("action");
        if ("getVoters".equalsIgnoreCase(action)) {
            handleGetCandidateVoters(request, response);
            return;
        }

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

    private void handleGetCandidateVoters(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String electionIdParam = request.getParameter("electionId");
        String candidateIdParam = request.getParameter("candidateId");

        long electionId = -1;
        long candidateId = -1;

        try {
            if (electionIdParam != null) electionId = Long.parseLong(electionIdParam.trim());
            if (candidateIdParam != null) candidateId = Long.parseLong(candidateIdParam.trim());
        } catch (NumberFormatException ignored) {}

        List<Voter> voters = new ArrayList<>();
        if (electionId > 0 && candidateId > 0) {
            voters = voteDAO.getVotersForCandidate(electionId, candidateId);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < voters.size(); i++) {
            Voter v = voters.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"voterId\":").append(v.getVoterId()).append(",")
                .append("\"name\":\"").append(escapeJson(v.getName())).append("\",")
                .append("\"email\":\"").append(escapeJson(v.getEmail())).append("\",")
                .append("\"status\":\"").append(escapeJson(v.getStatus())).append("\"")
                .append("}");
        }
        json.append("]");
        out.print(json.toString());
        out.flush();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String hex = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(hex.substring(hex.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
