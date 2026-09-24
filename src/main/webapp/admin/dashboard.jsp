<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- If accessed directly without going through AdminDashboardServlet, redirect to populate data --%>
<c:if test="${empty allElections && empty electionResultsMap}">
    <c:redirect url="/admin/dashboard" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Administrator Dashboard - Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="admin-page">

    <!-- Include Reusable Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container admin-dashboard-container">

        <!-- Admin Welcome Header -->
        <section class="admin-banner">
            <div class="admin-banner-text">
                <div class="banner-pill admin-pill">Election Commission Control Center</div>
                <h1 class="admin-banner-title">
                    Administrator Dashboard: <span class="gradient-text">${currentAdmin.name}</span>
                </h1>
                <p class="admin-banner-sub">
                    Institutional Email: <strong>${currentAdmin.email}</strong> &bull; 
                    Role: <span class="badge badge-purple">${currentAdmin.role}</span>
                </p>
            </div>
            <div class="admin-banner-actions">
                <span class="live-pulse-badge">
                    <span class="live-dot"></span> Live Tabulation Active
                </span>
                <a href="${pageContext.request.contextPath}/admin/logout" class="btn btn-outline-danger btn-sm" style="margin-left: 0.75rem;">
                    Admin Logout
                </a>
            </div>
        </section>

        <!-- Quick Governance Action Bar -->
        <section class="admin-quick-actions-bar">
            <a href="${pageContext.request.contextPath}/admin/elections" class="btn btn-primary">
                📅 Manage Elections
            </a>
            <a href="${pageContext.request.contextPath}/admin/candidates" class="btn btn-outline-primary">
                👥 Candidate Directory
            </a>
            <a href="${pageContext.request.contextPath}/admin/results" class="btn btn-purple">
                📊 Live Tally & Auditing
            </a>
        </section>

        <!-- Metric Stat Cards Grid -->
        <section class="metrics-grid">
            <div class="metric-card">
                <div class="metric-icon-wrap icon-blue">🗳️</div>
                <div class="metric-data">
                    <span class="metric-num">${not empty allElections ? allElections.size() : 0}</span>
                    <span class="metric-title">Total Elections</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-icon-wrap icon-emerald">⚡</div>
                <div class="metric-data">
                    <span class="metric-num">${not empty activeElections ? activeElections.size() : 0}</span>
                    <span class="metric-title">Active Polling Windows</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-icon-wrap icon-purple">📥</div>
                <div class="metric-data">
                    <span class="metric-num">${totalBallotsCast}</span>
                    <span class="metric-title">Total Ballots Cast</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-icon-wrap icon-amber">🔒</div>
                <div class="metric-data">
                    <span class="metric-num">100%</span>
                    <span class="metric-title">Cryptographic Integrity</span>
                </div>
            </div>
        </section>

        <!-- Active Elections Summaries & Governance Table -->
        <section class="admin-results-section">
            <div class="section-heading-wrap" style="display: flex; justify-content: space-between; align-items: flex-end;">
                <div>
                    <h2 class="section-heading">Electoral Operations Overview</h2>
                    <p class="section-subheading">Active and scheduled elections with real-time participation status.</p>
                </div>
                <a href="${pageContext.request.contextPath}/admin/elections" class="btn btn-sm btn-outline-secondary">
                    + Schedule New Election
                </a>
            </div>

            <c:choose>
                <c:when test="${not empty allElections}">
                    <div class="table-responsive" style="background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm);">
                        <table class="tally-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Election Title</th>
                                    <th>Status</th>
                                    <th>Polling Window</th>
                                    <th>Votes Tabulated</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="election" items="${allElections}">
                                    <c:set var="results" value="${electionResultsMap[election.electionId]}" />
                                    <c:set var="totalVotesForThis" value="0" />
                                    <c:forEach var="r" items="${results}">
                                        <c:set var="totalVotesForThis" value="${totalVotesForThis + r.totalVotes}" />
                                    </c:forEach>

                                    <tr>
                                        <td><strong>#${election.electionId}</strong></td>
                                        <td>
                                            <strong>${election.title}</strong>
                                            <div class="text-muted small">${election.description}</div>
                                        </td>
                                        <td>
                                            <span class="badge ${election.status == 'ACTIVE' ? 'badge-success' : (election.status == 'CLOSED' ? 'badge-danger' : 'badge-secondary')}">
                                                ${election.status}
                                            </span>
                                        </td>
                                        <td class="small">
                                            <div><strong>Start:</strong> ${election.startDate}</div>
                                            <div><strong>End:</strong> ${election.endDate}</div>
                                        </td>
                                        <td>
                                            <strong class="vote-count-number">${totalVotesForThis}</strong> ballots
                                        </td>
                                        <td>
                                            <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                                                <a href="${pageContext.request.contextPath}/admin/results?electionId=${election.electionId}" class="btn btn-sm btn-outline-primary">
                                                    Results
                                                </a>
                                                <a href="${pageContext.request.contextPath}/admin/candidates?electionId=${election.electionId}" class="btn btn-sm btn-outline-secondary">
                                                    Candidates
                                                </a>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="empty-state-card">
                        <div class="empty-icon">📊</div>
                        <h3>No Elections Recorded</h3>
                        <p>No election records found in the database. Schedule an election to begin tabulating votes.</p>
                        <a href="${pageContext.request.contextPath}/admin/elections" class="btn btn-primary" style="margin-top: 1rem;">
                            Schedule Election Now
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
