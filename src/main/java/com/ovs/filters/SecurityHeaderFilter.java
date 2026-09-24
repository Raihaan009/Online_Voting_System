package com.ovs.filters;

import com.ovs.util.CsrfUtil;
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
 * Enterprise Security Header & CSRF Initialization Filter.
 * Hardens HTTP response headers across the entire application footprint to thwart
 * MIME-sniffing, clickjacking (UI redressing), legacy cross-site scripting, and referrer leakage.
 * Automatically injects synchronizer CSRF tokens into authenticated sessions.
 */
@WebFilter("/*")
public class SecurityHeaderFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Lifecycle initialization hook
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse httpResponse) {
            // OWASP Security Hardening Headers
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        }

        if (request instanceof HttpServletRequest httpRequest) {
            // Provision or propagate CSRF token if session exists
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                String token = CsrfUtil.getOrCreateToken(session);
                httpRequest.setAttribute("csrfToken", token);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Lifecycle cleanup hook
    }
}
