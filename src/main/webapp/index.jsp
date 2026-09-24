<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Online Voting System - Enterprise 3-Tier MVC Architecture powered by Java 17 and Jakarta EE 10">
    <title>Online Voting System - Deployment Confirmation</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Header Navigation -->
    <header class="navbar">
        <div class="nav-container">
            <a href="${pageContext.request.contextPath}/" class="logo-brand">
                <div class="logo-badge">V</div>
                <span>Online Voting System</span>
            </a>
            <div class="nav-status">
                <span class="status-dot"></span>
                <span>Phase 1 Initialized</span>
            </div>
        </div>
    </header>

    <!-- Main Content Container -->
    <main class="container">

        <!-- Hero Section -->
        <section class="hero">
            <div class="hero-pill">Deployment Verified</div>
            <h1>Enterprise <span>Online Voting System</span></h1>
            <p class="hero-subtitle">
                Application skeleton successfully deployed on Apache Tomcat 10.1+ with Jakarta EE 10 and Java 17 LTS.
                The 3-tier MVC architecture is configured and ready for business domain modules.
            </p>
        </section>

        <!-- Technical Stack Specifications Grid -->
        <section class="specs-grid">
            <div class="spec-card">
                <div class="spec-icon blue">⚡</div>
                <div class="spec-title">Java Runtime</div>
                <div class="spec-value">Java 17 LTS (Java SE 17) with modern language features and records.</div>
                <div class="spec-tag">JVM: 17.0+</div>
            </div>

            <div class="spec-card">
                <div class="spec-icon purple">🚀</div>
                <div class="spec-title">Web Container</div>
                <div class="spec-value">Apache Tomcat 10.1+ supporting Jakarta Servlet API 6.0 and JSTL 3.0.</div>
                <div class="spec-tag">Jakarta EE 10</div>
            </div>

            <div class="spec-card">
                <div class="spec-icon amber">🗄️</div>
                <div class="spec-title">Persistence Tier</div>
                <div class="spec-value">MySQL Connector/J 8.3.0 connecting to <code>online_voting_db</code>.</div>
                <div class="spec-tag">Port 3306</div>
            </div>

            <div class="spec-card">
                <div class="spec-icon green">🔒</div>
                <div class="spec-title">Security & Cryptography</div>
                <div class="spec-value">jBCrypt 0.4 for cryptographically strong, salted voter password hashing.</div>
                <div class="spec-tag">jBCrypt Active</div>
            </div>
        </section>

        <!-- 3-Tier MVC Architecture Overview -->
        <section class="arch-container">
            <h2 class="section-title">
                <span>🏛️</span> 3-Tier MVC Architecture Structure
            </h2>
            <p class="hero-subtitle" style="text-align: left; margin: 0 0 1rem 0;">
                The codebase follows separation of concerns across presentation, business logic/controllers, and data access layers:
            </p>

            <div class="tier-grid">
                <!-- Tier 1: Presentation -->
                <div class="tier-box">
                    <div class="tier-header">
                        <span class="tier-name">1. Presentation Tier</span>
                        <span class="tier-badge badge-layer1">Views</span>
                    </div>
                    <ul class="tier-list">
                        <li><code>/WEB-INF/web.xml</code> (Servlet 6.0 Descriptor)</li>
                        <li><code>index.jsp</code> (Landing Page)</li>
                        <li><code>/css/style.css</code> (Responsive Stylesheet)</li>
                        <li><code>/js/main.js</code> (Client Interactions)</li>
                    </ul>
                </div>

                <!-- Tier 2: Controller & Service -->
                <div class="tier-box">
                    <div class="tier-header">
                        <span class="tier-name">2. Business Logic Tier</span>
                        <span class="tier-badge badge-layer2">Controllers</span>
                    </div>
                    <ul class="tier-list">
                        <li><code>com.ovs.controllers</code> (Servlets)</li>
                        <li><code>com.ovs.filters</code> (Auth & Session Filters)</li>
                        <li><code>com.ovs.models</code> (Domain Entities & DTOs)</li>
                        <li><code>com.ovs.util</code> (BCrypt & Helpers)</li>
                    </ul>
                </div>

                <!-- Tier 3: Data Access Layer -->
                <div class="tier-box">
                    <div class="tier-header">
                        <span class="tier-name">3. Data Access Tier</span>
                        <span class="tier-badge badge-layer3">Persistence</span>
                    </div>
                    <ul class="tier-list">
                        <li><code>com.ovs.config.DBConnection</code> (Singleton)</li>
                        <li><code>com.ovs.dao</code> (DAO Layer & Queries)</li>
                        <li><code>sql/schema.sql</code> (Database Definitions)</li>
                        <li>MySQL 8.x Database Engine</li>
                    </ul>
                </div>
            </div>
        </section>

        <!-- Database Connection Verification Guide -->
        <section class="arch-container">
            <h2 class="section-title">
                <span>🔌</span> Quick Database Verification
            </h2>
            <p style="color: var(--text-secondary); margin-bottom: 0.75rem;">
                To immediately verify your MySQL connection, run the Singleton main method in <code>DBConnection.java</code>:
            </p>

            <div class="code-box">
                <button class="copy-btn" onclick="copyToClipboard('code-test-cmd', this)">Copy Command</button>
                <pre id="code-test-cmd"># Test JDBC connection via Maven/Java:
java -cp target/classes;target/online-voting-system/WEB-INF/lib/* com.ovs.config.DBConnection</pre>
            </div>

            <div style="margin-top: 1rem; font-size: 0.85rem; color: var(--text-muted);">
                Target JDBC URL: <code>jdbc:mysql://localhost:3306/online_voting_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC</code>
            </div>
        </section>

    </main>

    <!-- Footer -->
    <footer>
        <p>&copy; 2026 Online Voting System. Enterprise 3-Tier MVC Architecture.</p>
        <p style="font-size: 0.8rem; margin-top: 0.35rem; color: var(--text-muted);">
            Client Time: <span id="client-timestamp">Loading...</span> | Context Path: <code>${pageContext.request.contextPath}</code>
        </p>
    </footer>

    <!-- Scripts -->
    <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>
