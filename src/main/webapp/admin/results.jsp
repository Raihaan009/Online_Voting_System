<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- If accessed directly without going through AdminResultsServlet, redirect --%>
<c:if test="${empty allElections}">
    <c:redirect url="/admin/results" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Live Tabulation & Audit - Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="admin-page">

    <!-- Include Reusable Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container admin-dashboard-container">

        <!-- Breadcrumb & Header -->
        <div class="admin-breadcrumb-bar">
            <a href="${pageContext.request.contextPath}/admin/dashboard">&larr; Back to Admin Dashboard</a>
        </div>

        <section class="admin-banner" style="margin-top: 1rem;">
            <div class="admin-banner-text">
                <div class="banner-pill admin-pill">Live Tabulation & Audit</div>
                <h1 class="admin-banner-title">Electoral Results & Audit Trail</h1>
                <p class="admin-banner-sub">Real-time candidate tallies aggregated directly from the secret ballot box with cryptographic proof verification.</p>
            </div>
            <div class="admin-banner-actions">
                <span class="live-pulse-badge">
                    <span class="live-dot"></span> Real-Time Aggregation
                </span>
            </div>
        </section>

        <!-- Election Selector Bar -->
        <section class="arch-container" style="margin-bottom: 2rem; padding: 1.25rem 1.75rem;">
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <label for="resultsElectionSelect" class="form-label" style="margin-bottom: 0.25rem;">Select Election for Tabulation:</label>
                    <select id="resultsElectionSelect" class="form-control" style="width: auto; min-width: 320px; font-weight: 600;" 
                            onchange="window.location.href='${pageContext.request.contextPath}/admin/results?electionId=' + this.value">
                        <c:forEach var="el" items="${allElections}">
                            <option value="${el.electionId}" ${el.electionId == selectedElectionId ? 'selected' : ''}>
                                #${el.electionId}: ${el.title} (${el.status})
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <c:if test="${not empty selectedElection}">
                    <div style="display: flex; gap: 0.5rem; align-items: center;">
                        <span class="badge ${selectedElection.status == 'ACTIVE' ? 'badge-success' : 'badge-secondary'}">
                            ${selectedElection.status}
                        </span>
                        <button type="button" onclick="window.print()" class="btn btn-sm btn-outline-secondary">
                            🖨️ Export / Print Tally
                        </button>
                    </div>
                </c:if>
            </div>
        </section>

        <c:choose>
            <c:when test="${not empty selectedElection}">
                <!-- High-Level Election Metrics Grid -->
                <section class="metrics-grid">
                    <div class="metric-card">
                        <div class="metric-icon-wrap icon-blue">👥</div>
                        <div class="metric-data">
                            <span class="metric-num">${analytics.totalEligibleVoters}</span>
                            <span class="metric-title">Eligible Electorate</span>
                        </div>
                    </div>

                    <div class="metric-card">
                        <div class="metric-icon-wrap icon-purple">📥</div>
                        <div class="metric-data">
                            <span class="metric-num">${analytics.totalVotesCast}</span>
                            <span class="metric-title">Ballots Tabulated</span>
                        </div>
                    </div>

                    <div class="metric-card">
                        <div class="metric-icon-wrap icon-emerald">📊</div>
                        <div class="metric-data">
                            <span class="metric-num">
                                <c:out value="${String.format('%.1f', analytics.turnoutPercentage)}" />%
                            </span>
                            <span class="metric-title">Voter Turnout</span>
                        </div>
                    </div>

                    <div class="metric-card">
                        <div class="metric-icon-wrap icon-amber">🏆</div>
                        <div class="metric-data">
                            <span class="metric-num" style="font-size: 1.15rem; word-break: break-word;">
                                <c:out value="${analytics.leadingCandidate}" />
                            </span>
                            <span class="metric-title">Current Leader</span>
                        </div>
                    </div>
                </section>

                <!-- Tabular Candidate Standings -->
                <section class="admin-results-section" style="margin-bottom: 2.5rem;">
                    <div class="election-tally-card">
                        <div class="tally-card-header">
                            <div>
                                <h2 class="tally-election-title">${selectedElection.title}</h2>
                                <p class="tally-election-desc">${selectedElection.description}</p>
                                <small class="text-muted">
                                    Official Polling Window: ${selectedElection.startDate} &mdash; ${selectedElection.endDate}
                                </small>
                            </div>
                            <div class="tally-summary-pill">
                                <span class="pill-label">Total Valid Votes</span>
                                <span class="pill-number">${analytics.totalVotesCast}</span>
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="tally-table">
                                <thead>
                                    <tr>
                                        <th>Rank</th>
                                        <th>Candidate</th>
                                        <th>Party & Symbol</th>
                                        <th>Votes Received</th>
                                        <th>Vote Share</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty analytics.candidateBreakdown}">
                                            <c:forEach var="cand" items="${analytics.candidateBreakdown}" varStatus="status">
                                                <tr class="${status.first && cand.totalVotes > 0 ? 'leader-row' : ''}">
                                                    <td class="rank-col">
                                                        <c:choose>
                                                            <c:when test="${status.first && cand.totalVotes > 0}">
                                                                <span class="trophy-badge">🏆 1st (Leader)</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                #${status.count}
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="candidate-col">
                                                        <strong>${cand.candidateName}</strong>
                                                    </td>
                                                    <td class="symbol-col">
                                                        <span class="symbol-tag">${cand.partySymbol}</span>
                                                    </td>
                                                    <td class="votes-col">
                                                        <strong class="vote-count-number">${cand.totalVotes}</strong> votes
                                                    </td>
                                                    <td class="share-col">
                                                        <div class="progress-bar-wrap">
                                                            <div class="progress-bar-fill" data-progress="${cand.voteShare}"></div>
                                                        </div>
                                                        <span class="percentage-label">
                                                            <c:out value="${String.format('%.1f', cand.voteShare)}" />%
                                                        </span>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" class="text-center text-muted">
                                                    No candidate nominees recorded for this election.
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <!-- Cryptographic Receipt Token Audit Search Box -->
                <section class="arch-container" style="background: #0f172a; color: #e2e8f0; border: 1px solid #1e293b;">
                    <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.85rem;">
                        <span style="font-size: 1.5rem;">🔍</span>
                        <div>
                            <h3 style="color: #f8fafc; font-size: 1.2rem; font-weight: 800;">
                                Cryptographic Ballot Audit Trail Verification
                            </h3>
                            <p style="color: #94a3b8; font-size: 0.85rem;">
                                Enter a student's 64-character SHA-256 digital receipt token to verify that their ballot was legitimately recorded in this election's secret ballot box:
                            </p>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/admin/results" method="GET" style="display: flex; gap: 0.75rem; flex-wrap: wrap; margin-bottom: 1.25rem;">
                        <input type="hidden" name="electionId" value="${selectedElection.electionId}">
                        <input type="text" name="receiptToken" class="form-control" 
                               value="${auditTokenSearched}" 
                               placeholder="e.g. 0ffe2a845b41dc4368f839e12cc58e5370865460e2103747592f12805da8b402" 
                               style="flex: 1; min-width: 300px; font-family: var(--font-mono); font-size: 0.85rem; background: #020617; border-color: #334155; color: #38bdf8;" required>
                        <button type="submit" class="btn btn-primary">
                            Verify Ballot Audit Trail &rarr;
                        </button>
                    </form>

                    <!-- Audit Outcome Display -->
                    <c:if test="${not empty auditTokenSearched}">
                        <c:choose>
                            <c:when test="${auditTokenValid}">
                                <div class="alert alert-success" style="background: rgba(16, 185, 129, 0.15); border-color: rgba(16, 185, 129, 0.4); color: #34d399;">
                                    <span class="alert-icon">✓</span>
                                    <div class="alert-content">
                                        <strong>Cryptographic Audit Verified:</strong>
                                        A secret ballot with receipt token <code>${auditTokenSearched}</code> was legitimately recorded in the database.
                                        <c:if test="${not empty auditDetails}">
                                            <div style="margin-top: 0.5rem; font-size: 0.85rem; color: #e2e8f0;">
                                                Ballot ID: <strong>#${auditDetails.voteId}</strong> &bull; 
                                                Candidate: <strong>${auditDetails.candidateName} (${auditDetails.partySymbol})</strong> &bull; 
                                                Timestamp: <strong>${auditDetails.timestamp}</strong>
                                            </div>
                                        </c:if>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="alert alert-danger" style="background: rgba(239, 68, 68, 0.15); border-color: rgba(239, 68, 68, 0.4); color: #f87171;">
                                    <span class="alert-icon">✗</span>
                                    <div class="alert-content">
                                        <strong>Verification Failed:</strong>
                                        No ballot matching receipt token <code>${auditTokenSearched}</code> was found in Election #${selectedElection.electionId}.
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </section>
            </c:when>
            <c:otherwise>
                <div class="empty-state-card">
                    <div class="empty-icon">📊</div>
                    <h3>No Elections to Tabulate</h3>
                    <p>There are no recorded elections available to display results for.</p>
                </div>
            </c:otherwise>
        </c:choose>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
