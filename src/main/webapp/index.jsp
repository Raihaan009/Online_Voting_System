<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="CampusVote - Enterprise 3-Tier MVC Architecture powered by Java 17, Jakarta EE 10, and MySQL">
    <title>CampusVote | Online Voting System - Democratic Campus Elections</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Include Reusable Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <!-- Main Landing Container -->
    <main class="container">

        <!-- Hero Section -->
        <section class="hero-banner">
            <canvas id="heroParticleCanvas"></canvas>
            <div class="hero-content-wrap">
                <div style="display: inline-flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem; background: var(--surface-card); padding: 0.4rem 1rem; border-radius: 9999px; border: 1px solid var(--border-color); box-shadow: var(--shadow-sm);">
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
                    <span style="font-weight: 800; font-size: 0.95rem; color: var(--text-primary);">CampusVote</span>
                    <span style="font-size: 0.68rem; font-weight: 700; letter-spacing: 1px; color: var(--text-muted); text-transform: uppercase;">ONLINE VOTING SYSTEM</span>
                </div>

                <h1 class="hero-headline">
                    Secure, Transparent & <span class="gradient-text">Verifiable Elections</span>
                </h1>
                <p class="hero-lead">
                    An enterprise 3-tier electronic voting platform designed for university student councils, academic senates, and institutional governance. 
                    Guarantees democratic ballot secrecy, eliminates double-voting, and issues cryptographic audit receipts.
                </p>
            <div class="hero-actions">
                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser}">
                        <a href="${pageContext.request.contextPath}/voter/dashboard" class="btn btn-primary btn-lg">
                            Go to Voter Dashboard &rarr;
                        </a>
                    </c:when>
                    <c:when test="${not empty sessionScope.currentAdmin}">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-primary btn-lg">
                            Go to Admin Console &rarr;
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-primary btn-lg">
                            Access Voter Portal &rarr;
                        </a>
                        <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-outline-secondary btn-lg">
                            Register as Voter
                        </a>
                        <a href="${pageContext.request.contextPath}/verify-receipt" class="btn btn-outline-secondary btn-lg">
                            🔍 Verify Ballot Receipt
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
            </div>
        </section>

        <!-- Core Feature Cards Grid -->
        <section class="portal-grid">
            <!-- Card 1: Cast Your Vote -->
            <div class="portal-card">
                <div class="portal-icon-wrap icon-blue">🗳️</div>
                <h3 class="portal-title">Cast Your Vote</h3>
                <p class="portal-desc">
                    Eligible students and institutional members can inspect active democratic elections, review candidates and manifestos, and submit an immutable ballot.
                </p>
                <div class="portal-features">
                    <span>✓ Multi-election ballot support</span>
                    <span>✓ One-person-one-vote rule</span>
                    <span>✓ Instant digital receipt confirmation</span>
                </div>
                <a href="${pageContext.request.contextPath}/login.jsp" class="portal-link">
                    Open Voting Booth &rarr;
                </a>
            </div>

            <!-- Card 2: Admin Portal -->
            <div class="portal-card">
                <div class="portal-icon-wrap icon-purple">🛡️</div>
                <h3 class="portal-title">Admin Portal</h3>
                <p class="portal-desc">
                    Election commissioners and administrators can monitor turnout in real-time, inspect automated vote tallies, and oversee election integrity.
                </p>
                <div class="portal-features">
                    <span>✓ Live ballot count aggregation</span>
                    <span>✓ Real-time voter turnout metrics</span>
                    <span>✓ Role-based access governance</span>
                </div>
                <a href="${pageContext.request.contextPath}/admin/login.jsp" class="portal-link">
                    Admin Access Console &rarr;
                </a>
            </div>

            <!-- Card 3: Integrity & Verification -->
            <div class="portal-card">
                <div class="portal-icon-wrap icon-emerald">🔒</div>
                <h3 class="portal-title">Election Integrity</h3>
                <p class="portal-desc">
                    Built upon the principle of democratic secret ballots. The voting engine decouples voter identity from cast choices and generates verifiable cryptographic proofs.
                </p>
                <div class="portal-features">
                    <span>✓ SHA-256 cryptographic tokens</span>
                    <span>✓ ACID database transactions</span>
                    <span>✓ Zero voter identity leakage</span>
                </div>
                <div class="portal-tag" style="margin-bottom: 0.75rem;">
                    End-to-End Auditable
                </div>
                <a href="${pageContext.request.contextPath}/verify-receipt" class="portal-link" style="color: #059669;">
                    Verify Ballot Receipt &rarr;
                </a>
            </div>
        </section>

        <!-- Architecture & Democratic Principles Showcase -->
        <section class="integrity-section">
            <div class="integrity-header">
                <h2>Democratic Safeguards & System Principles</h2>
                <p>Every ballot cast through the Online Voting System is protected by architectural guarantees:</p>
            </div>

            <div class="safeguards-grid">
                <div class="safeguard-box">
                    <span class="safeguard-icon">🕶️</span>
                    <h4>Ballot Secrecy</h4>
                    <p>The votes table records only the election ID, candidate selection, and receipt token. Voter identity is strictly omitted from the ballot box.</p>
                </div>

                <div class="safeguard-box">
                    <span class="safeguard-icon">⚖️</span>
                    <h4>One-Person-One-Vote</h4>
                    <p>Enforced at both the controller and database transaction layers. Any duplicate attempt to vote triggers an automatic rollback.</p>
                </div>

                <div class="safeguard-box">
                    <span class="safeguard-icon">📜</span>
                    <h4>Auditable Digital Receipt</h4>
                    <p>Upon voting, voters receive a unique 64-character SHA-256 cryptographic receipt confirming their ballot was committed without revealing their vote.</p>
                </div>

                <div class="safeguard-box">
                    <span class="safeguard-icon">📊</span>
                    <h4>Instant Tabulation</h4>
                    <p>Eliminates manual vote counting errors. SQL aggregation instantly computes candidate standings without human intervention.</p>
                </div>
            </div>
        </section>

    </main>

    <!-- Include Reusable Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
