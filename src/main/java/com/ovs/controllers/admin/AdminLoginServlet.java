package com.ovs.controllers.admin;

import com.ovs.dao.AdminDAO;
import com.ovs.models.Admin;
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
 * Administrative Authentication Controller.
 * Authenticates election administrators against the MySQL database with BCrypt password verification,
 * guards against session fixation, and initializes the administrative session context.
 */
@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private AdminDAO adminDAO;
    private com.ovs.dao.AuditDAO auditDAO;

    @Override
    public void init() throws ServletException {
        this.adminDAO = new AdminDAO();
        this.auditDAO = new com.ovs.dao.AuditDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("currentAdmin") != null) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/admin/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            String errMsg = URLEncoder.encode("Email and password cannot be empty.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/login.jsp?error=" + errMsg);
            return;
        }

        email = email.trim().toLowerCase();

        Admin admin = adminDAO.login(email, password);
        if (admin != null) {
            // Mitigate session fixation by destroying prior session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Provision a fresh authenticated administrative session
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("currentAdmin", admin);
            newSession.setMaxInactiveInterval(30 * 60); // 30 minutes session timeout

            auditDAO.logAction(admin.getEmail(), "ADMIN_LOGIN", 
                    "Successful administrator authentication. Role: " + admin.getRole(), 
                    request.getRemoteAddr());

            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            auditDAO.logAction(email, "ADMIN_LOGIN_FAILED", 
                    "Failed administrative authentication attempt.", 
                    request.getRemoteAddr());

            String errMsg = URLEncoder.encode("Invalid administrator credentials. Access denied.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/login.jsp?error=" + errMsg);
        }
    }
}
