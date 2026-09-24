package com.ovs.filters;

import com.ovs.models.Admin;
import com.ovs.models.Voter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication and Security Filter for the Online Voting System.
 * Intercepts protected voter and administrative endpoints to guarantee role-based access control (RBAC),
 * session validation, and strict HTTP anti-caching security policies.
 */
@WebFilter(urlPatterns = {"/voter/*", "/admin/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization lifecycle hook
    }

    /**
     * Intercepts incoming requests to protected routes, enforces authentication,
     * and sets HTTP cache control headers to prevent unauthorized page history access.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Prevent client and proxy caching of sensitive authenticated pages (OWASP A01:2021)
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        httpResponse.setHeader("Pragma", "no-cache");                                  // HTTP 1.0
        httpResponse.setDateHeader("Expires", 0);                                       // Proxies

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = requestURI.substring(contextPath.length());

        HttpSession session = httpRequest.getSession(false);

        // 1. Administrative Security Perimeter (/admin/*)
        if (relativePath.startsWith("/admin/") || relativePath.equals("/admin")) {
            // Exclude public administrative authentication endpoints from filtering
            if (relativePath.equals("/admin/login.jsp") || relativePath.equals("/admin/login")) {
                chain.doFilter(request, response);
                return;
            }

            Admin admin = (session != null) ? (Admin) session.getAttribute("currentAdmin") : null;
            if (admin == null) {
                httpResponse.sendRedirect(contextPath + "/admin/login.jsp?error=unauthorized");
                return;
            }
        }

        // 2. Voter Security Perimeter (/voter/*)
        if (relativePath.startsWith("/voter/") || relativePath.equals("/voter")) {
            Voter voter = (session != null) ? (Voter) session.getAttribute("currentUser") : null;
            if (voter == null) {
                httpResponse.sendRedirect(contextPath + "/login.jsp?error=unauthorized");
                return;
            }
        }

        // Proceed to next filter or target servlet resource
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Resource cleanup lifecycle hook
    }
}
