<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- Global Application Footer -->
<footer class="site-footer">
    <div class="footer-container">
        <div class="footer-grid">
            <!-- Brand & Mission Column -->
            <div class="footer-col">
                <div class="footer-brand">
                    <div class="logo-badge" aria-label="CampusVote Emblem" style="display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; background: var(--accent-light); border-radius: var(--radius-md); border: 1px solid var(--border-color);">
                        <svg class="brand-emblem-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width: 20px; height: 20px;">
                            <path d="M3 9L12 3L21 9V10H3V9Z" fill="currentColor" fill-opacity="0.15"/>
                            <path d="M3 10H21"/>
                            <path d="M6 10V17"/>
                            <path d="M10 10V17"/>
                            <path d="M14 10V17"/>
                            <path d="M18 10V17"/>
                            <path d="M2 17H22V20H2V17Z"/>
                            <path d="M9 13.5L11 15.5L15 11.5" stroke="currentColor" stroke-width="2.2"/>
                        </svg>
                    </div>
                    <div class="footer-brand-text">
                        <span class="footer-brand-name">CampusVote</span>
                        <span class="footer-brand-sub brand-secondary-text">ONLINE VOTING SYSTEM</span>
                    </div>
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
                &copy; 2026 CampusVote &bull; Online Voting System. Academic Micro-Project Submission for Full Stack Java Programming (FSJP).
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
