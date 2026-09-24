package com.ovs.controllers.admin;

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
 * Administrative Session Termination Controller.
 * Clears administrator session tokens and returns the user to the admin login portal.
 */
@WebServlet("/admin/logout")
public class AdminLogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("currentAdmin");
            session.invalidate();
        }

        String msg = URLEncoder.encode("You have been securely logged out of the administrative console.", StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/admin/login.jsp?info=" + msg);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
