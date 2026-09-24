package com.ovs.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Cross-Site Request Forgery (CSRF) Cryptographic Protection Utility.
 * Implements the Synchronizer Token Pattern using CSPRNG (SecureRandom) 256-bit tokens
 * with constant-time byte comparison to thwart side-channel timing attacks.
 */
public class CsrfUtil {

    public static final String CSRF_SESSION_ATTR = "csrfToken";
    public static final String CSRF_PARAM_NAME = "csrfToken";
    public static final String CSRF_HEADER_NAME = "X-CSRF-Token";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a new 64-character hexadecimal cryptographically secure random token.
     *
     * @return 256-bit hex token
     */
    public static String generateToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return HexFormat.of().formatHex(tokenBytes);
    }

    /**
     * Retrieves the existing CSRF token from the session, or provisions a new one if not present.
     *
     * @param session active HTTP session
     * @return 64-character hex CSRF token
     */
    public static String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return "";
        }
        String token = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (token == null || token.trim().isEmpty()) {
            token = generateToken();
            session.setAttribute(CSRF_SESSION_ATTR, token);
        }
        return token;
    }

    /**
     * Validates whether the incoming request contains a valid CSRF token
     * matching the authenticated session's token.
     * Checks form parameters followed by custom request headers.
     *
     * @param request incoming HTTP servlet request
     * @return true if token matches identically via constant-time comparison, false otherwise
     */
    public static boolean isValidToken(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }

        String requestToken = request.getParameter(CSRF_PARAM_NAME);
        if (requestToken == null || requestToken.trim().isEmpty()) {
            requestToken = request.getHeader(CSRF_HEADER_NAME);
        }

        if (requestToken == null || requestToken.trim().isEmpty()) {
            return false;
        }

        // Constant-time byte array comparison prevents timing attack analysis
        byte[] a = sessionToken.trim().getBytes(StandardCharsets.UTF_8);
        byte[] b = requestToken.trim().getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
