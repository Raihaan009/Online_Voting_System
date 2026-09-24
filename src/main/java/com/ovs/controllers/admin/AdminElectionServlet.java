package com.ovs.controllers.admin;

import com.ovs.dao.ElectionDAO;
import com.ovs.models.Election;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller handling election lifecycle governance:
 * Scheduling new elections, updating polling statuses (ACTIVE, SCHEDULED, CLOSED),
 * and managing election deletions.
 */
@WebServlet("/admin/elections")
public class AdminElectionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private ElectionDAO electionDAO;

    @Override
    public void init() throws ServletException {
        this.electionDAO = new ElectionDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Election> elections = electionDAO.getAllElections();
        request.setAttribute("elections", elections);
        request.getRequestDispatcher("/admin/elections.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("create".equalsIgnoreCase(action)) {
            handleCreateElection(request, response);
        } else if ("updateStatus".equalsIgnoreCase(action)) {
            handleUpdateStatus(request, response);
        } else if ("delete".equalsIgnoreCase(action)) {
            handleDeleteElection(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/elections");
        }
    }

    private void handleCreateElection(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String status = request.getParameter("status");

        if (title == null || title.trim().isEmpty()) {
            String err = URLEncoder.encode("Election title is required.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/elections?error=" + err);
            return;
        }

        LocalDateTime startDate = parseDateTime(startDateStr, LocalDateTime.now());
        LocalDateTime endDate = parseDateTime(endDateStr, LocalDateTime.now().plusDays(7));

        if (status == null || status.trim().isEmpty()) {
            status = "ACTIVE";
        }

        Election election = new Election();
        election.setTitle(title.trim());
        election.setDescription(description != null ? description.trim() : "");
        election.setStartDate(startDate);
        election.setEndDate(endDate);
        election.setStatus(status.trim().toUpperCase());

        boolean created = electionDAO.createElection(election);
        if (created) {
            String msg = URLEncoder.encode("New election '" + election.getTitle() + "' provisioned successfully.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/elections?success=" + msg);
        } else {
            String err = URLEncoder.encode("Failed to create election due to database error.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/elections?error=" + err);
        }
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String electionIdParam = request.getParameter("electionId");
        String status = request.getParameter("status");

        if (electionIdParam != null && status != null) {
            try {
                long electionId = Long.parseLong(electionIdParam.trim());
                boolean updated = electionDAO.updateElectionStatus(electionId, status.trim().toUpperCase());
                if (updated) {
                    String msg = URLEncoder.encode("Election status updated to " + status + ".", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/elections?success=" + msg);
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        String err = URLEncoder.encode("Could not update election status.", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/elections?error=" + err);
    }

    private void handleDeleteElection(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String electionIdParam = request.getParameter("electionId");
        if (electionIdParam != null) {
            try {
                long electionId = Long.parseLong(electionIdParam.trim());
                boolean deleted = electionDAO.deleteElection(electionId);
                if (deleted) {
                    String msg = URLEncoder.encode("Election #" + electionId + " and associated records deleted.", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/elections?success=" + msg);
                    return;
                }
            } catch (NumberFormatException ignored) {}
        }

        String err = URLEncoder.encode("Failed to delete election.", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/elections?error=" + err);
    }

    private LocalDateTime parseDateTime(String raw, LocalDateTime fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return LocalDateTime.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(raw.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return fallback;
            }
        }
    }
}
