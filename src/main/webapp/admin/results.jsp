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
    <title>Live Tabulation & Audit - CampusVote | Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
                                <c:choose>
                                    <c:when test="${not empty analytics.turnoutFormatted}">
                                        <c:out value="${analytics.turnoutFormatted}" />%
                                    </c:when>
                                    <c:when test="${not empty analytics.turnoutPercentage}">
                                        <c:out value="${analytics.turnoutPercentage}" />%
                                    </c:when>
                                    <c:otherwise>0.0%</c:otherwise>
                                </c:choose>
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

                <!-- 3. Live Analytics & Dual-Theme Interactive Charts -->
                <section class="admin-results-section" style="margin-bottom: 2.5rem;">
                    <div class="analytics-charts-grid">
                        <!-- Chart 1: Candidate Vote Share Donut / Bar Chart -->
                        <div class="chart-card">
                            <div class="chart-card-header">
                                <h3 class="chart-card-title">🗳️ Candidate Vote Shares</h3>
                                <span class="badge badge-success" style="font-size: 0.75rem;">Live Tabulation</span>
                            </div>
                            <!-- Skeleton loader while initializing -->
                            <div id="chartSkeleton1" class="chart-canvas-container" style="display: flex; flex-direction: column; gap: 0.75rem; justify-content: center; align-items: center;">
                                <div class="skeleton-circle" style="width: 140px; height: 140px;"></div>
                                <div class="skeleton-text" style="width: 60%;"></div>
                            </div>
                            <div class="chart-canvas-container" id="chartWrapper1" style="display: none;">
                                <canvas id="candidateShareChart"></canvas>
                            </div>
                        </div>

                        <!-- Chart 2: Turnout by Academic Department Breakdown -->
                        <div class="chart-card">
                            <div class="chart-card-header">
                                <h3 class="chart-card-title">🏛️ Turnout by Department</h3>
                                <span class="badge badge-purple" style="font-size: 0.75rem;">Demographic Audit</span>
                            </div>
                            <!-- Skeleton loader while initializing -->
                            <div id="chartSkeleton2" class="chart-canvas-container" style="display: flex; flex-direction: column; gap: 1rem; justify-content: center;">
                                <div class="skeleton-text" style="height: 24px; width: 90%;"></div>
                                <div class="skeleton-text" style="height: 24px; width: 75%;"></div>
                                <div class="skeleton-text" style="height: 24px; width: 60%;"></div>
                                <div class="skeleton-text" style="height: 24px; width: 45%;"></div>
                            </div>
                            <div class="chart-canvas-container" id="chartWrapper2" style="display: none;">
                                <canvas id="departmentTurnoutChart"></canvas>
                            </div>
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
                                                        <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; flex-wrap: wrap;">
                                                            <span><strong class="vote-count-number">${cand.totalVotes}</strong> votes</span>
                                                            <button type="button" 
                                                                    class="btn btn-sm btn-outline-primary view-voters-btn"
                                                                    data-candidate-id="${cand.candidateId}"
                                                                    data-election-id="${selectedElectionId}"
                                                                    data-candidate-name="<c:out value="${cand.candidateName}"/>"
                                                                    data-candidate-symbol="<c:out value="${cand.partySymbol}"/>"
                                                                    data-total-votes="${cand.totalVotes}">
                                                                👥 View Voters
                                                            </button>
                                                        </div>
                                                    </td>
                                                    <td class="share-col">
                                                        <div class="progress-bar-wrap">
                                                            <div class="progress-bar-fill" data-progress="${cand.voteShare}"></div>
                                                        </div>
                                                        <span class="percentage-label">
                                                            <c:choose>
                                                                <c:when test="${not empty cand.voteShareFormatted}">
                                                                    <c:out value="${cand.voteShareFormatted}" />%
                                                                </c:when>
                                                                <c:when test="${not empty cand.voteShare}">
                                                                    <c:out value="${cand.voteShare}" />%
                                                                </c:when>
                                                                <c:otherwise>0.0%</c:otherwise>
                                                            </c:choose>
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
                <section class="arch-container audit-search-card" style="margin-bottom: 2.5rem;">
                    <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.85rem;">
                        <span style="font-size: 1.5rem;">🔍</span>
                        <div>
                            <h3 style="color: var(--text-primary); font-size: 1.2rem; font-weight: 800; margin: 0 0 0.25rem 0;">
                                Cryptographic Ballot Audit Trail Verification
                            </h3>
                            <p style="color: var(--text-muted); font-size: 0.85rem; margin: 0;">
                                Enter a student's 64-character SHA-256 digital receipt token to verify that their ballot was legitimately recorded in this election's secret ballot box:
                            </p>
                        </div>
                    </div>

                    <form action="${pageContext.request.contextPath}/admin/results" method="GET" style="display: flex; gap: 0.75rem; flex-wrap: wrap; margin-bottom: 1.25rem;">
                        <input type="hidden" name="electionId" value="${selectedElection.electionId}">
                        <input type="text" name="receiptToken" class="form-control" 
                               value="${auditTokenSearched}" 
                               placeholder="e.g. 0ffe2a845b41dc4368f839e12cc58e5370865460e2103747592f12805da8b402" 
                               style="flex: 1; min-width: 300px; font-family: var(--font-mono); font-size: 0.85rem;" required>
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

        <!-- Candidate Voter Breakdown Audit Modal (On-Demand Admin Audit) -->
        <div id="candidateVotersModal" class="modal-overlay" style="display: none;">
            <div class="modal-card" style="max-width: 680px; width: 95%;">
                <div class="modal-header" style="text-align: left; display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                    <div>
                        <div class="badge badge-purple" style="margin-bottom: 0.35rem;">On-Demand Admin Audit</div>
                        <h3 class="modal-title" id="modalVotersCandidateTitle" style="font-size: 1.3rem;">Candidate Voters Breakdown</h3>
                        <p class="modal-warning" id="modalVotersCandidateSubtitle" style="margin-bottom: 0; font-size: 0.85rem;">
                            Electoral audit of voters who voted for this nominee.
                        </p>
                    </div>
                    <button type="button" id="closeVotersModalBtnX" class="btn-outline-secondary" style="border: none; background: transparent; font-size: 1.5rem; cursor: pointer; line-height: 1; color: var(--text-secondary);" aria-label="Close">&times;</button>
                </div>

                <div class="modal-body" style="max-height: 380px; overflow-y: auto;">
                    <div id="votersLoadingIndicator" style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
                        <div style="font-size: 2rem; margin-bottom: 0.5rem;">⏳</div>
                        <p style="font-size: 0.95rem;">Retrieving candidate voter breakdown from audit trail...</p>
                    </div>

                    <div id="votersTableWrap" style="display: none;">
                        <table class="tally-table" style="font-size: 0.875rem;">
                            <thead>
                                <tr>
                                    <th style="width: 50px;">#</th>
                                    <th>Voter Full Name</th>
                                    <th>Institutional Email</th>
                                    <th style="width: 100px;">Status</th>
                                </tr>
                            </thead>
                            <tbody id="votersTableBody">
                            </tbody>
                        </table>
                    </div>

                    <div id="votersEmptyMessage" class="alert alert-info" style="display: none; margin: 1rem 0;">
                        <span class="alert-icon">ℹ️</span>
                        <div class="alert-content">No ballots have been recorded for this candidate in this election yet.</div>
                    </div>
                </div>

                <div class="modal-footer" style="margin-top: 1.25rem;">
                    <button type="button" id="closeVotersModalBtn" class="btn btn-outline-secondary">
                        Close Audit Window
                    </button>
                </div>
            </div>
        </div>

    </main>

    <script>
    document.addEventListener("DOMContentLoaded", function () {
        var modal = document.getElementById("candidateVotersModal");
        var closeBtnX = document.getElementById("closeVotersModalBtnX");
        var closeBtn = document.getElementById("closeVotersModalBtn");
        var titleEl = document.getElementById("modalVotersCandidateTitle");
        var subTitleEl = document.getElementById("modalVotersCandidateSubtitle");
        var loadingEl = document.getElementById("votersLoadingIndicator");
        var tableWrap = document.getElementById("votersTableWrap");
        var tbody = document.getElementById("votersTableBody");
        var emptyEl = document.getElementById("votersEmptyMessage");

        function closeModal() {
            if (modal) modal.style.display = "none";
        }

        if (closeBtnX) closeBtnX.addEventListener("click", closeModal);
        if (closeBtn) closeBtn.addEventListener("click", closeModal);
        if (modal) {
            modal.addEventListener("click", function (e) {
                if (e.target === modal) closeModal();
            });
        }

        document.querySelectorAll(".view-voters-btn").forEach(function (btn) {
            btn.addEventListener("click", function () {
                var candidateId = this.getAttribute("data-candidate-id");
                var electionId = this.getAttribute("data-election-id");
                var candidateName = this.getAttribute("data-candidate-name") || "Candidate";
                var candidateSymbol = this.getAttribute("data-candidate-symbol") || "";
                var totalVotes = this.getAttribute("data-total-votes") || "0";

                if (titleEl) {
                    titleEl.textContent = candidateName + (candidateSymbol ? " (" + candidateSymbol + ")" : "");
                }
                if (subTitleEl) {
                    subTitleEl.textContent = "Total Audited Ballots: " + totalVotes + " votes recorded in secret ballot box.";
                }

                if (loadingEl) loadingEl.style.display = "block";
                if (tableWrap) tableWrap.style.display = "none";
                if (emptyEl) emptyEl.style.display = "none";
                if (tbody) tbody.innerHTML = "";
                if (modal) modal.style.display = "flex";

                var url = "${pageContext.request.contextPath}/admin/results?action=getVoters&electionId=" + 
                          encodeURIComponent(electionId) + "&candidateId=" + encodeURIComponent(candidateId);

                fetch(url)
                    .then(function (res) { return res.json(); })
                    .then(function (voters) {
                        if (loadingEl) loadingEl.style.display = "none";
                        if (!voters || voters.length === 0) {
                            if (emptyEl) emptyEl.style.display = "flex";
                        } else {
                            var html = "";
                            voters.forEach(function (v, idx) {
                                html += "<tr>" +
                                        "<td>#" + (idx + 1) + "</td>" +
                                        "<td><strong>" + escapeHtml(v.name) + "</strong></td>" +
                                        "<td><code>" + escapeHtml(v.email) + "</code></td>" +
                                        "<td><span class='badge badge-success'>" + escapeHtml(v.status) + "</span></td>" +
                                        "</tr>";
                            });
                            if (tbody) tbody.innerHTML = html;
                            if (tableWrap) tableWrap.style.display = "block";
                        }
                    })
                    .catch(function (err) {
                        if (loadingEl) loadingEl.style.display = "none";
                        if (emptyEl) {
                            emptyEl.textContent = "Error loading audit records: " + err.message;
                            emptyEl.style.display = "flex";
                        }
                    });
            });
        });

        function escapeHtml(str) {
            if (!str) return "";
            return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
        }

        // =========================================================================
        // Chart.js Dual-Theme Integration & Dynamic Color Hook
        // =========================================================================
        var candChart = null;
        var deptChart = null;

        function getChartTheme(isDark) {
            return {
                textColor: isDark ? '#F8FAFC' : '#19212A',
                mutedColor: isDark ? '#94A3B8' : '#5E6D7E',
                gridColor: isDark ? '#334155' : '#DDE2E5',
                cardBg: isDark ? '#1E293B' : '#FFFFFF',
                palette: ['#0D5C75', '#38BDF8', '#059669', '#D97706', '#7C3AED', '#E11D48', '#2563EB', '#10B981']
            };
        }

        function initOrUpdateCharts() {
            var isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            var theme = getChartTheme(isDark);

            var candLabels = ${not empty candLabelsJson ? candLabelsJson : '[]'};
            var candVotes = ${not empty candVotesJson ? candVotesJson : '[]'};
            var deptLabels = ${not empty deptLabelsJson ? deptLabelsJson : '[]'};
            var deptCounts = ${not empty deptCountsJson ? deptCountsJson : '[]'};

            // Swap skeleton loaders with active chart canvas
            var sk1 = document.getElementById('chartSkeleton1');
            var cw1 = document.getElementById('chartWrapper1');
            if (sk1 && cw1) { sk1.style.display = 'none'; cw1.style.display = 'block'; }

            var sk2 = document.getElementById('chartSkeleton2');
            var cw2 = document.getElementById('chartWrapper2');
            if (sk2 && cw2) { sk2.style.display = 'none'; cw2.style.display = 'block'; }

            // 1. Candidate Vote Share Donut Chart
            var ctx1 = document.getElementById('candidateShareChart');
            if (ctx1 && typeof Chart !== 'undefined') {
                var hasVotes = candVotes && candVotes.length > 0 && candVotes.some(function(v) { return v > 0; });
                var displayLabels = (candLabels && candLabels.length > 0) ? candLabels : ['No Nominees Yet'];
                var displayVotes = (candVotes && candVotes.length > 0) ? candVotes : [1];
                if (!hasVotes && (!candVotes || candVotes.length === 0)) {
                    displayLabels = ['No Votes Cast Yet'];
                    displayVotes = [1];
                }

                if (candChart) {
                    candChart.options.plugins.legend.labels.color = theme.textColor;
                    candChart.options.plugins.tooltip.backgroundColor = isDark ? '#0F172A' : '#19212A';
                    candChart.options.plugins.tooltip.borderColor = theme.gridColor;
                    candChart.data.datasets[0].borderColor = theme.cardBg;
                    candChart.data.labels = displayLabels;
                    candChart.data.datasets[0].data = displayVotes;
                    candChart.update();
                } else {
                    candChart = new Chart(ctx1.getContext('2d'), {
                        type: 'doughnut',
                        data: {
                            labels: displayLabels,
                            datasets: [{
                                data: displayVotes,
                                backgroundColor: theme.palette,
                                borderColor: theme.cardBg,
                                borderWidth: 2
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: {
                                legend: {
                                    position: 'bottom',
                                    labels: { color: theme.textColor, font: { family: "'Inter', sans-serif", weight: '600', size: 12 } }
                                },
                                tooltip: {
                                    backgroundColor: isDark ? '#0F172A' : '#19212A',
                                    titleColor: '#F8FAFC',
                                    bodyColor: '#F8FAFC',
                                    borderColor: theme.gridColor,
                                    borderWidth: 1
                                }
                            }
                        }
                    });
                }
            }

            // 2. Turnout by Academic Department Horizontal Bar Chart
            var ctx2 = document.getElementById('departmentTurnoutChart');
            if (ctx2 && typeof Chart !== 'undefined') {
                var dLabels = (deptLabels && deptLabels.length > 0) ? deptLabels : ['Computer Science', 'Mechanical', 'Business', 'Electrical'];
                var dCounts = (deptCounts && deptCounts.length > 0) ? deptCounts : [0, 0, 0, 0];

                if (deptChart) {
                    deptChart.options.scales.x.ticks.color = theme.mutedColor;
                    deptChart.options.scales.x.grid.color = theme.gridColor;
                    deptChart.options.scales.y.ticks.color = theme.textColor;
                    deptChart.options.plugins.tooltip.backgroundColor = isDark ? '#0F172A' : '#19212A';
                    deptChart.options.plugins.tooltip.borderColor = theme.gridColor;
                    deptChart.data.datasets[0].backgroundColor = isDark ? '#38BDF8' : '#0D5C75';
                    deptChart.data.labels = dLabels;
                    deptChart.data.datasets[0].data = dCounts;
                    deptChart.update();
                } else {
                    deptChart = new Chart(ctx2.getContext('2d'), {
                        type: 'bar',
                        data: {
                            labels: dLabels,
                            datasets: [{
                                label: 'Turnout (Ballots Cast)',
                                data: dCounts,
                                backgroundColor: isDark ? '#38BDF8' : '#0D5C75',
                                borderRadius: 6
                            }]
                        },
                        options: {
                            indexAxis: 'y',
                            responsive: true,
                            maintainAspectRatio: false,
                            scales: {
                                x: {
                                    beginAtZero: true,
                                    ticks: { color: theme.mutedColor, stepSize: 1, precision: 0 },
                                    grid: { color: theme.gridColor }
                                },
                                y: {
                                    ticks: { color: theme.textColor, font: { weight: '600' } },
                                    grid: { display: false }
                                }
                            },
                            plugins: {
                                legend: { display: false },
                                tooltip: {
                                    backgroundColor: isDark ? '#0F172A' : '#19212A',
                                    titleColor: '#F8FAFC',
                                    bodyColor: '#F8FAFC',
                                    borderColor: theme.gridColor,
                                    borderWidth: 1
                                }
                            }
                        }
                    });
                }
            }
        }

        setTimeout(initOrUpdateCharts, 200);
        window.addEventListener('campusvote:themechange', initOrUpdateCharts);
    });
    </script>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
