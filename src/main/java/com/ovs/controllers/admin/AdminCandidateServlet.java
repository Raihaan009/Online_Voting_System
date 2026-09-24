package com.ovs.controllers.admin;

import com.ovs.dao.CandidateDAO;
import com.ovs.dao.ElectionDAO;
import com.ovs.models.Candidate;
import com.ovs.models.Election;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller managing candidate nominations and registry:
 * Attaching candidate profiles, symbols, and manifestos to elections,
 * and processing candidate deletions.
 */
@WebServlet("/admin/candidates")
public class AdminCandidateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ElectionDAO electionDAO;
    private CandidateDAO candidateDAO;
    private com.ovs.dao.AuditDAO auditDAO;

    @Override
    public void init() throws ServletException {
        this.electionDAO = new ElectionDAO();
        this.candidateDAO = new CandidateDAO();
        this.auditDAO = new com.ovs.dao.AuditDAO();
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
            List<Candidate> candidates = candidateDAO.getCandidatesByElection(selectedElectionId);
            request.setAttribute("selectedElectionId", selectedElectionId);
            request.setAttribute("selectedElection", selectedElection);
            request.setAttribute("candidates", candidates);
        }

        request.getRequestDispatcher("/admin/candidates.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Validate CSRF Protection Token
        if (!com.ovs.util.CsrfUtil.isValidToken(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Security Validation Failed: Invalid or missing CSRF token.");
            return;
        }

        String action = request.getParameter("action");

        if ("add".equalsIgnoreCase(action)) {
            handleAddCandidate(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            handleDeleteCandidate(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/candidates");
        }
    }

    private void handleAddCandidate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String name = request.getParameter("name");
        String partySymbol = request.getParameter("partySymbol");
        String manifesto = request.getParameter("manifesto");
        String electionIdParam = request.getParameter("electionId");

        if (name == null || name.trim().isEmpty() || electionIdParam == null || electionIdParam.trim().isEmpty()) {
            String err = URLEncoder.encode("Candidate name and target election are required.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/candidates?error=" + err);
            return;
        }

        try {
            long electionId = Long.parseLong(electionIdParam.trim());
            Candidate candidate = new Candidate();
            candidate.setName(name.trim());
            candidate.setPartySymbol(partySymbol != null ? partySymbol.trim() : "Independent");
            candidate.setManifesto(manifesto != null ? manifesto.trim() : "");
            candidate.setElectionId(electionId);
            candidate.setStatus("APPROVED");

            boolean added = candidateDAO.addCandidate(candidate);
            if (added) {
                com.ovs.models.Admin admin = (com.ovs.models.Admin) request.getSession().getAttribute("currentAdmin");
                String adminEmail = (admin != null) ? admin.getEmail() : "admin";
                auditDAO.logAction(adminEmail, "NOMINATE_CANDIDATE", 
                        "Nominated candidate '" + candidate.getName() + "' (" + candidate.getPartySymbol() + ") for Election #" + electionId, 
                        request.getRemoteAddr());

                String msg = URLEncoder.encode("Candidate '" + candidate.getName() + "' nominated successfully.", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/admin/candidates?electionId=" + electionId + "&success=" + msg);
                return;
            }
        } catch (NumberFormatException ignored) {}

        String err = URLEncoder.encode("Failed to register candidate nomination.", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/candidates?error=" + err);
    }

    private void handleDeleteCandidate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String candidateIdParam = request.getParameter("candidateId");
        String electionIdParam = request.getParameter("electionId");

        if (candidateIdParam != null) {
            try {
                long candidateId = Long.parseLong(candidateIdParam.trim());
                boolean deleted = candidateDAO.deleteCandidate(candidateId);
                if (deleted) {
                    com.ovs.models.Admin admin = (com.ovs.models.Admin) request.getSession().getAttribute("currentAdmin");
                    String adminEmail = (admin != null) ? admin.getEmail() : "admin";
                    auditDAO.logAction(adminEmail, "DELETE_CANDIDATE", 
                            "Removed Candidate #" + candidateId + " from Election #" + electionIdParam, 
                            request.getRemoteAddr());

                    String msg = URLEncoder.encode("Candidate #" + candidateId + " removed.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/candidates?electionId=" + electionIdParam + "&success=" + msg);
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        String err = URLEncoder.encode("Failed to delete candidate.", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/candidates?error=" + err);
    }
}
