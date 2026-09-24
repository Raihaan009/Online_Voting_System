package com.ovs.controllers;

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
 * Controller handling user and administrator logout operations.
 * Clears authenticated session attributes, invalidates the server-side HTTP session,
 * and securely redirects to the login view.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Handles GET request to logout user, invalidate active session, and redirect to login page.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("currentUser");
            session.removeAttribute("currentAdmin");
            session.removeAttribute("receiptToken");
            session.invalidate();
        }

        String msg = URLEncoder.encode("You have been successfully logged out.", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/login.jsp?info=" + msg);
    }

    /**
     * Handles POST logout requests by delegating to doGet.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
