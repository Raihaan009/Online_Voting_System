package com.ovs.controllers;

import com.ovs.dao.AuditDAO;
import com.ovs.dao.VoterDAO;
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

/**
 * Controller managing voter authentication and session provisioning.
 * Validates credentials using {@link VoterDAO#login(String, String)},
 * guards against session fixation, audits voter authentication activity,
 * and redirects authorized voters to the dashboard.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private VoterDAO voterDAO;
    private AuditDAO auditDAO;

    @Override
    public void init() throws ServletException {
        this.voterDAO = new VoterDAO();
        this.auditDAO = new AuditDAO();
    }

    /**
     * Handles GET requests. If voter is already authenticated, redirects directly
     * to the voter dashboard; otherwise redirects to the login view.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            response.sendRedirect(request.getContextPath() + "/voter/dashboard");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    /**
     * Handles POST submission of voter login credentials.
     * Authenticates via BCrypt password verification, establishes a secure session,
     * logs authentication audit trail, and routes to the dashboard.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String clientIp = request.getRemoteAddr();

        // 1. Validate parameter presence
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            auditDAO.logAction(
                    (email != null && !email.trim().isEmpty()) ? email.trim().toLowerCase() : "anonymous",
                    "VOTER_LOGIN_FAILED",
                    "Rejected authentication attempt: Missing credentials",
                    clientIp);

            String errMsg = URLEncoder.encode("Email and password cannot be empty.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
            return;
        }

        email = email.trim().toLowerCase();

        // 2. Perform authentication check
        Voter voter = voterDAO.login(email, password);

        if (voter != null) {
            // Verify account status
            if ("SUSPENDED".equalsIgnoreCase(voter.getStatus()) || "REJECTED".equalsIgnoreCase(voter.getStatus())) {
                auditDAO.logAction(voter.getEmail(), "VOTER_LOGIN_FAILED",
                        "Authentication blocked: Account status is " + voter.getStatus(),
                        clientIp);

                String errMsg = URLEncoder.encode("Your voter registration is currently suspended or rejected. Please contact an election official.", StandardCharsets.UTF_8);
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
                return;
            }

            // Invalidate any existing session to mitigate session fixation attacks
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Create new authenticated session
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("currentUser", voter);
            newSession.setMaxInactiveInterval(30 * 60); // 30 minutes inactivity timeout

            // Record successful voter login audit log
            auditDAO.logAction(voter.getEmail(), "VOTER_LOGIN",
                    "Successful voter authentication. Status: " + voter.getStatus() + ", Name: " + voter.getName(),
                    clientIp);

            response.sendRedirect(request.getContextPath() + "/voter/dashboard");
        } else {
            // Record failed voter login audit log
            auditDAO.logAction(email, "VOTER_LOGIN_FAILED",
                    "Failed voter authentication: Invalid credentials",
                    clientIp);

            String errMsg = URLEncoder.encode("Invalid email or password. Please verify your credentials.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
        }
    }
}
