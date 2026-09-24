<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- Global Application Footer -->
<footer class="site-footer">
    <div class="footer-container">
        <div class="footer-grid">
            <!-- Brand & Mission Column -->
            <div class="footer-col">
                <div class="footer-brand">
                    <span class="footer-logo-badge">V</span>
                    <span class="footer-brand-name">Online Voting System</span>
                </div>
                <p class="footer-desc">
                    Enterprise 3-Tier MVC democratic electronic voting platform engineered with Java 17 LTS, Jakarta EE 10, and MySQL. 
                    Guaranteeing end-to-end secret ballot privacy, one-person-one-vote validation, and cryptographic auditability.
                </p>
            </div>

            <!-- Architectural Standards Column -->
            <div class="footer-col">
                <h4 class="footer-heading">Architecture & Security</h4>
                <ul class="footer-list">
                    <li><span class="bullet">🔒</span> Secret Ballot Cryptographic Decoupling</li>
                    <li><span class="bullet">🛡️</span> BCrypt 12-Round Password Hashing</li>
                    <li><span class="bullet">⚡</span> ACID-Compliant Transactional Engine</li>
                    <li><span class="bullet">📜</span> SHA-256 Digital Verification Receipts</li>
                </ul>
            </div>

            <!-- System Status Badges Column -->
            <div class="footer-col">
                <h4 class="footer-heading">Runtime Environment</h4>
                <div class="footer-tags">
                    <span class="footer-tag">Java 17 LTS</span>
                    <span class="footer-tag">Jakarta EE 10</span>
                    <span class="footer-tag">Tomcat 10.1+</span>
                    <span class="footer-tag">MySQL 8.0+</span>
                    <span class="footer-tag">JSTL 3.0</span>
                </div>
                <div class="footer-time-wrap">
                    <span class="status-indicator-dot"></span>
                    <span>System Time: <strong id="client-timestamp">Synchronizing...</strong></span>
                </div>
            </div>
        </div>

        <div class="footer-bottom">
            <div class="footer-copy">
                &copy; 2026 Online Voting System. Academic Micro-Project Submission for Full Stack Java Programming (FSJP).
            </div>
            <div class="footer-links">
                <a href="${pageContext.request.contextPath}/">Home</a>
                <span class="divider">|</span>
                <a href="${pageContext.request.contextPath}/login.jsp">Voter Portal</a>
                <span class="divider">|</span>
                <a href="${pageContext.request.contextPath}/admin/login.jsp">Admin Console</a>
            </div>
        </div>
    </div>
</footer>

<!-- Application Scripts -->
<script src="${pageContext.request.contextPath}/js/main.js"></script>
