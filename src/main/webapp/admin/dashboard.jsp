<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<%-- If accessed directly without going through AdminServlet, redirect to populate data --%>
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
                    Logged in as: <strong>${currentAdmin.email}</strong> &bull; 
                    Role: <span class="badge badge-purple">${currentAdmin.role}</span>
                </p>
            </div>
            <div class="admin-banner-actions">
                <span class="live-pulse-badge">
                    <span class="live-dot"></span> Live Tabulation Active
                </span>
            </div>
        </section>

        <!-- Calculate Aggregated Global Metrics -->
        <c:set var="totalVotesAcrossElections" value="0" />
        <c:forEach var="election" items="${allElections}">
            <c:set var="results" value="${electionResultsMap[election.electionId]}" />
            <c:forEach var="res" items="${results}">
                <c:set var="totalVotesAcrossElections" value="${totalVotesAcrossElections + res.totalVotes}" />
            </c:forEach>
        </c:forEach>

        <!-- Metric Stat Cards Grid -->
        <section class="metrics-grid">
            <div class="metric-card">
                <div class="metric-icon-wrap icon-blue">🗳️</div>
                <div class="metric-data">
                    <span class="metric-num">${not empty allElections ? allElections.size() : 0}</span>
                    <span class="metric-title">Total Scheduled Elections</span>
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
                    <span class="metric-num">${totalVotesAcrossElections}</span>
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

        <!-- Tabulated Results by Election -->
        <section class="admin-results-section">
            <div class="section-heading-wrap">
                <div>
                    <h2 class="section-heading">Live Election Tabulations & Results</h2>
                    <p class="section-subheading">Candidate vote totals are tallied in real time via SQL aggregation from the secret ballot box.</p>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty allElections}">
                    <c:forEach var="election" items="${allElections}">
                        <c:set var="results" value="${electionResultsMap[election.electionId]}" />

                        <!-- Compute Election Total Votes -->
                        <c:set var="electionTotalVotes" value="0" />
                        <c:forEach var="res" items="${results}">
                            <c:set var="electionTotalVotes" value="${electionTotalVotes + res.totalVotes}" />
                        </c:forEach>

                        <div class="election-tally-card">
                            <div class="tally-card-header">
                                <div>
                                    <div class="tally-title-wrap">
                                        <h3 class="tally-election-title">${election.title}</h3>
                                        <span class="badge ${election.status == 'ACTIVE' ? 'badge-success' : 'badge-secondary'}">
                                            ${election.status}
                                        </span>
                                    </div>
                                    <p class="tally-election-desc">${election.description}</p>
                                    <small class="text-muted">
                                        Polling Window: ${election.startDate} &mdash; ${election.endDate}
                                    </small>
                                </div>
                                <div class="tally-summary-pill">
                                    <span class="pill-label">Total Ballots</span>
                                    <span class="pill-number">${electionTotalVotes}</span>
                                </div>
                            </div>

                            <!-- Tabular Results Display -->
                            <div class="table-responsive">
                                <table class="tally-table">
                                    <thead>
                                        <tr>
                                            <th>Rank</th>
                                            <th>Candidate</th>
                                            <th>Affiliation / Symbol</th>
                                            <th>Votes Tabulated</th>
                                            <th>Vote Share</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${not empty results}">
                                                <c:forEach var="res" items="${results}" varStatus="status">
                                                    <c:set var="percentage" value="0" />
                                                    <c:if test="${electionTotalVotes > 0}">
                                                        <c:set var="percentage" value="${(res.totalVotes * 100.0) / electionTotalVotes}" />
                                                    </c:if>

                                                    <tr class="${status.first && res.totalVotes > 0 ? 'leader-row' : ''}">
                                                        <td class="rank-col">
                                                            <c:choose>
                                                                <c:when test="${status.first && res.totalVotes > 0}">
                                                                    <span class="trophy-badge">🏆 1st</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    #${status.count}
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="candidate-col">
                                                            <strong>${res.candidateName}</strong>
                                                        </td>
                                                        <td class="symbol-col">
                                                            <span class="symbol-tag">${res.partySymbol}</span>
                                                        </td>
                                                        <td class="votes-col">
                                                            <strong class="vote-count-number">${res.totalVotes}</strong> votes
                                                        </td>
                                                        <td class="share-col">
                                                            <div class="progress-bar-wrap">
                                                                <div class="progress-bar-fill" style="width: ${percentage}%;"></div>
                                                            </div>
                                                            <span class="percentage-label">
                                                                <c:out value="${String.format('%.1f', percentage)}" />%
                                                            </span>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <tr>
                                                    <td colspan="5" class="text-center text-muted">
                                                        No candidate results registered for this election.
                                                    </td>
                                                </tr>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:forEach>
                </c:when>
                <c:otherwise>
                    <div class="empty-state-card">
                        <div class="empty-icon">📊</div>
                        <h3>No Elections Recorded</h3>
                        <p>No election records found in the database. Schedule an election to begin tabulating votes.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
