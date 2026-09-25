package com.ovs.controllers;

import com.ovs.dao.AuditDAO;
import com.ovs.dao.VoterDAO;
import com.ovs.models.Voter;
import com.ovs.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Controller managing mandatory voter profile completion and verification.
 * Dispatches the profile setup view, validates student institutional details,
 * updates the database to mark profile as complete, and syncs session state.
 */
@WebServlet("/voter/profile")
public class VoterProfileServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final List<String> VALID_ACADEMIC_YEARS = Arrays.asList(
            "First Year (FE)",
            "Second Year (SE)",
            "Third Year (TE)",
            "Final Year (BE)"
    );

    private static final List<String> VALID_BRANCHES = Arrays.asList(
            "Computer Engineering",
            "Information Technology",
            "AI & Data Science",
            "Electronics & Telecommunication (EXTC)",
            "Mechanical Engineering",
            "Civil Engineering",
            "Electrical Engineering"
    );

    private VoterDAO voterDAO;
    private AuditDAO auditDAO;

    @Override
    public void init() throws ServletException {
        this.voterDAO = new VoterDAO();
        this.auditDAO = new AuditDAO();
    }

    /**
     * Renders the voter profile management screen, pre-populated with existing
     * institutional information from the database.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Voter sessionVoter = (session != null) ? (Voter) session.getAttribute("currentUser") : null;

        if (sessionVoter == null) {
            String errMsg = URLEncoder.encode("Please log in to manage your voter profile.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
            return;
        }

        // Fetch fresh voter data from database to ensure accuracy
        Voter voter = voterDAO.findById(sessionVoter.getVoterId());
        if (voter == null) {
            voter = sessionVoter;
        } else {
            session.setAttribute("currentUser", voter);
        }

        request.setAttribute("voter", voter);
        request.setAttribute("academicYears", VALID_ACADEMIC_YEARS);
        request.setAttribute("branches", VALID_BRANCHES);

        request.getRequestDispatcher("/voter/profile.jsp").forward(request, response);
    }

    /**
     * Processes submission of voter profile attributes with strict server-side validation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Voter voter = (session != null) ? (Voter) session.getAttribute("currentUser") : null;

        if (voter == null) {
            String errMsg = URLEncoder.encode("Please log in to complete your profile.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/login.jsp?error=" + errMsg);
            return;
        }

        // 1. CSRF Verification
        if (!CsrfUtil.isValidToken(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Security Validation Failed: Invalid or missing CSRF token.");
            return;
        }

        // 2. Extract and Sanitize Parameters
        String name = request.getParameter("name");
        String ageStr = request.getParameter("age");
        String academicYear = request.getParameter("academicYear");
        String branch = request.getParameter("branch");

        // Prepare attributes for form redisplay on validation error
        request.setAttribute("academicYears", VALID_ACADEMIC_YEARS);
        request.setAttribute("branches", VALID_BRANCHES);

        // 3. Validation Logic
        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("error", "Full Name is required and cannot be blank.");
            request.setAttribute("voter", voter);
            request.getRequestDispatcher("/voter/profile.jsp").forward(request, response);
            return;
        }
        name = name.trim();

        int age;
        try {
            if (ageStr == null || ageStr.trim().isEmpty()) {
                throw new NumberFormatException("Age is required.");
            }
            age = Integer.parseInt(ageStr.trim());
            if (age < 17 || age > 99) {
                request.setAttribute("error", "Age must be a valid number between 17 and 99.");
                request.setAttribute("voter", voter);
                request.getRequestDispatcher("/voter/profile.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Please enter a valid numeric age between 17 and 99.");
            request.setAttribute("voter", voter);
            request.getRequestDispatcher("/voter/profile.jsp").forward(request, response);
            return;
        }

        if (academicYear == null || academicYear.trim().isEmpty()) {
            request.setAttribute("error", "Please select your current Academic Year.");
            request.setAttribute("voter", voter);
            request.getRequestDispatcher("/voter/profile.jsp").forward(request, response);
            return;
        }
        academicYear = academicYear.trim();

        if (branch == null || branch.trim().isEmpty()) {
            request.setAttribute("error", "Please select your Academic Department / Branch.");
            request.setAttribute("voter", voter);
            request.getRequestDispatcher("/voter/profile.jsp").forward(request, response);
            return;
        }
        branch = branch.trim();

        // 4. Update Profile in Database
        boolean updated = voterDAO.updateVoterProfile(voter.getVoterId(), name, age, academicYear, branch);

        if (updated) {
            // Update session voter object
            voter.setName(name);
            voter.setAge(age);
            voter.setAcademicYear(academicYear);
            voter.setBranch(branch);
            voter.setDepartment(branch);
            voter.setProfileComplete(true);
            session.setAttribute("currentUser", voter);

            // Audit profile completion
            auditDAO.logAction(voter.getEmail(), "VOTER_PROFILE_COMPLETED",
                    "Official student profile completed: " + name + " (" + academicYear + " - " + branch + ", Age: " + age + ")",
                    request.getRemoteAddr());

            String successMsg = URLEncoder.encode("Your official student profile has been completed and verified! You are now eligible to vote in all active elections.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/voter/dashboard?success=" + successMsg);
        } else {
            request.setAttribute("error", "A database error occurred while updating your profile. Please try again.");
            request.setAttribute("voter", voter);
            request.getRequestDispatcher("/voter/profile.jsp").forward(request, response);
        }
    }
}
