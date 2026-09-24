package com.ovs.controllers;

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
 * Controller handling administrator authentication and secure session creation.
 * Validates administrative credentials against {@link AdminDAO}, applies session fixation
 * protection, and redirects to the administrative dashboard.
 */
@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private AdminDAO adminDAO;

    @Override
    public void init() throws ServletException {
        this.adminDAO = new AdminDAO();
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
            // Invalidate old session to mitigate session fixation attacks
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Create new administrative session
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("currentAdmin", admin);
            newSession.setMaxInactiveInterval(30 * 60); // 30 minutes timeout

            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            String errMsg = URLEncoder.encode("Invalid administrator credentials. Access denied.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/login.jsp?error=" + errMsg);
        }
    }
}
