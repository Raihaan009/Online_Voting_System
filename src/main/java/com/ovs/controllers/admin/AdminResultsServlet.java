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
import java.util.HashMap;
import java.util.LinkedHashMap;
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

        List<Election> allElections = new ArrayList<>();
        try {
            allElections = electionDAO.getAllElections();
        } catch (Exception e) {
            System.err.println("Error fetching elections in AdminResultsServlet: " + e.getMessage());
            e.printStackTrace();
        }
        if (allElections == null) {
            allElections = new ArrayList<>();
        }
        request.setAttribute("allElections", allElections);

        String electionIdParam = request.getParameter("electionId");
        long selectedElectionId = -1;

        if (electionIdParam != null && !electionIdParam.trim().isEmpty()) {
            try {
                selectedElectionId = Long.parseLong(electionIdParam.trim());
            } catch (NumberFormatException e) {
                selectedElectionId = -1;
            }
        }

        // Attempt to find selected election
        Election selectedElection = null;
        if (selectedElectionId > 0) {
            try {
                selectedElection = electionDAO.getElectionById(selectedElectionId);
            } catch (Exception e) {
                System.err.println("Error fetching election by ID (" + selectedElectionId + "): " + e.getMessage());
                e.printStackTrace();
            }
        }

        // If requested electionId is non-existent, invalid, or omitted, fallback gracefully to first available election
        if (selectedElection == null && !allElections.isEmpty()) {
            selectedElection = allElections.get(0);
            selectedElectionId = selectedElection.getElectionId();
        }

        Map<String, Object> analytics = new HashMap<>();
        Map<String, Integer> departmentTurnout = new LinkedHashMap<>();
        String candLabelsJson = "[]";
        String candVotesJson = "[]";
        String deptLabelsJson = "[]";
        String deptCountsJson = "[]";

        if (selectedElection != null) {
            try {
                analytics = voteDAO.getElectionAnalytics(selectedElectionId);
            } catch (Exception e) {
                System.err.println("Error retrieving election analytics for election " + selectedElectionId + ": " + e.getMessage());
                e.printStackTrace();
            }
            if (analytics == null) {
                analytics = new HashMap<>();
            }

            try {
                departmentTurnout = voteDAO.getTurnoutByDepartment(selectedElectionId);
            } catch (Exception e) {
                System.err.println("Error retrieving department turnout for election " + selectedElectionId + ": " + e.getMessage());
                e.printStackTrace();
            }
            if (departmentTurnout == null) {
                departmentTurnout = new LinkedHashMap<>();
            }

            // 1. Format clean JSON attributes for Candidate Results (List<CandidateResult>)
            @SuppressWarnings("unchecked")
            List<com.ovs.models.CandidateResult> candResults = 
                (List<com.ovs.models.CandidateResult>) analytics.get("candidateResults");
            if (candResults == null) {
                candResults = new ArrayList<>();
            }

            StringBuilder cLabels = new StringBuilder("[");
            StringBuilder cVotes = new StringBuilder("[");
            for (int i = 0; i < candResults.size(); i++) {
                com.ovs.models.CandidateResult c = candResults.get(i);
                if (c == null) continue;
                if (cLabels.length() > 1) {
                    cLabels.append(",");
                    cVotes.append(",");
                }
                cLabels.append("\"").append(escapeJson(c.getCandidateName())).append("\"");
                cVotes.append(c.getTotalVotes());
            }
            cLabels.append("]");
            cVotes.append("]");
            candLabelsJson = cLabels.toString();
            candVotesJson = cVotes.toString();

            // 2. Format Turnout by Department JSON
            StringBuilder dLabels = new StringBuilder("[");
            StringBuilder dCounts = new StringBuilder("[");
            int dIdx = 0;
            for (Map.Entry<String, Integer> entry : departmentTurnout.entrySet()) {
                if (dIdx > 0) {
                    dLabels.append(",");
                    dCounts.append(",");
                }
                dLabels.append("\"").append(escapeJson(entry.getKey())).append("\"");
                dCounts.append(entry.getValue());
                dIdx++;
            }
            dLabels.append("]");
            dCounts.append("]");
            deptLabelsJson = dLabels.toString();
            deptCountsJson = dCounts.toString();
        } else {
            // Safe baseline fallback when no elections exist
            analytics.put("totalEligibleVoters", 0L);
            analytics.put("totalVotesCast", 0L);
            analytics.put("turnoutPercentage", 0.0);
            analytics.put("turnoutFormatted", "0.0");
            analytics.put("candidateResults", new ArrayList<com.ovs.models.CandidateResult>());
            analytics.put("candidateBreakdown", new ArrayList<Map<String, Object>>());
            analytics.put("leadingCandidate", "None");
        }

        request.setAttribute("selectedElectionId", selectedElectionId);
        request.setAttribute("selectedElection", selectedElection);
        request.setAttribute("analytics", analytics);
        request.setAttribute("departmentTurnout", departmentTurnout);
        request.setAttribute("candLabelsJson", candLabelsJson);
        request.setAttribute("candVotesJson", candVotesJson);
        request.setAttribute("deptLabelsJson", deptLabelsJson);
        request.setAttribute("deptCountsJson", deptCountsJson);

        // Handle Cryptographic Receipt Audit Search
        String receiptToken = request.getParameter("receiptToken");
        if (receiptToken != null && !receiptToken.trim().isEmpty()) {
            receiptToken = receiptToken.trim();
            boolean isValid = false;
            Map<String, Object> auditDetails = null;
            try {
                isValid = voteDAO.verifyReceiptToken(selectedElectionId, receiptToken);
                auditDetails = voteDAO.getReceiptAuditDetails(receiptToken);
            } catch (Exception e) {
                System.err.println("Error verifying receipt token: " + e.getMessage());
                e.printStackTrace();
            }

            request.setAttribute("auditTokenSearched", receiptToken);
            request.setAttribute("auditTokenValid", isValid);
            request.setAttribute("auditDetails", auditDetails);
        }

        request.getRequestDispatcher("/admin/results.jsp").forward(request, response);
    }

    private void handleGetCandidateVoters(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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
            try {
                voters = voteDAO.getVotersForCandidate(electionId, candidateId);
            } catch (Exception e) {
                System.err.println("Error fetching voters for candidate (" + candidateId + ") in election (" + electionId + "): " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (voters == null) {
            voters = new ArrayList<>();
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
