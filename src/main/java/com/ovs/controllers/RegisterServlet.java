package com.ovs.controllers;

import com.ovs.dao.VoterDAO;
import com.ovs.models.Voter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Controller handling new institutional voter registration.
 * Validates form parameters, ensures unique institutional email addresses,
 * and delegates encrypted credential storage to {@link VoterDAO}.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private VoterDAO voterDAO;

    @Override
    public void init() throws ServletException {
        this.voterDAO = new VoterDAO();
    }

    /**
     * Handles GET requests by redirecting or forwarding to the voter registration view.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/register.jsp");
    }

    /**
     * Handles POST submission of voter registration details.
     * Validates input fields, checks email uniqueness, and registers the voter.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // 1. Input presence and format validation
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {

            String errMsg = URLEncoder.encode("All registration fields are required.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=" + errMsg);
            return;
        }

        name = name.trim();
        email = email.trim().toLowerCase();

        // 2. Validate email syntax
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            String errMsg = URLEncoder.encode("Please provide a valid institutional email address.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=" + errMsg);
            return;
        }

        // 3. Confirm password equality check (if provided in view)
        if (confirmPassword != null && !password.equals(confirmPassword)) {
            String errMsg = URLEncoder.encode("Passwords do not match. Please re-enter.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=" + errMsg);
            return;
        }

        // 4. Password complexity minimum length check
        if (password.length() < 6) {
            String errMsg = URLEncoder.encode("Password must be at least 6 characters long.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=" + errMsg);
            return;
        }

        // 5. Uniqueness validation: verify email does not already exist
        Voter existingVoter = voterDAO.getVoterByEmail(email);
        if (existingVoter != null) {
            String errMsg = URLEncoder.encode("An account with this email address is already registered.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=" + errMsg);
            return;
        }

        // 6. Build voter entity and persist (VoterDAO automatically hashes password with BCrypt)
        Voter newVoter = new Voter();
        newVoter.setName(name);
        newVoter.setEmail(email);
        newVoter.setPasswordHash(password);
        newVoter.setHasVoted(false);
        newVoter.setStatus("APPROVED");

        boolean isRegistered = voterDAO.registerVoter(newVoter);
        if (isRegistered) {
            String successMsg = URLEncoder.encode("Registration successful! You may now log in to participate.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?success=" + successMsg);
        } else {
            String errMsg = URLEncoder.encode("Failed to complete registration due to an internal system error.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=" + errMsg);
        }
    }
}
