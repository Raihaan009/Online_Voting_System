package com.ovs.controllers.admin;

import com.ovs.dao.AuditDAO;
import com.ovs.models.AuditLog;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Administrative Governance Controller for reviewing the immutable security audit log.
 * Exposes timestamps, administrator identities, operational actions, and client IPs.
 */
@WebServlet("/admin/audit")
public class AdminAuditServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private AuditDAO auditDAO;

    @Override
    public void init() throws ServletException {
        this.auditDAO = new AuditDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int limit = 100;
        String limitParam = request.getParameter("limit");
        if (limitParam != null && !limitParam.trim().isEmpty()) {
            try {
                int parsed = Integer.parseInt(limitParam.trim());
                if (parsed > 0 && parsed <= 500) {
                    limit = parsed;
                }
            } catch (NumberFormatException ignored) {}
        }

        List<AuditLog> auditLogs = auditDAO.getRecentLogs(limit);
        long totalLogCount = auditDAO.getTotalLogCount();

        request.setAttribute("auditLogs", auditLogs);
        request.setAttribute("totalLogCount", totalLogCount);
        request.setAttribute("currentLimit", limit);

        request.getRequestDispatcher("/admin/audit.jsp").forward(request, response);
    }
}
