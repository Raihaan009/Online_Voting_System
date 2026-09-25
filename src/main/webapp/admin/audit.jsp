<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- If accessed directly without going through AdminAuditServlet, redirect --%>
<c:if test="${empty adminLogs && empty voterLogs && empty totalLogCount}">
    <c:redirect url="/admin/audit" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Security Audit Trail - CampusVote | Online Voting System</title>
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
                <div class="banner-pill admin-pill">Enterprise Governance</div>
                <h1 class="admin-banner-title">Security & Operations Audit Trail</h1>
                <p class="admin-banner-sub">Immutable, timestamped record of administrative interventions, provisioning, and voter authentication telemetry.</p>
            </div>
            <div class="admin-banner-actions">
                <span class="live-pulse-badge">
                    <span class="live-dot"></span> Live Ledger Active
                </span>
            </div>
        </section>

        <!-- Summary Metrics Grid -->
        <section class="metrics-grid" style="margin-bottom: 2rem;">
            <div class="metric-card">
                <div class="metric-icon-wrap icon-blue">🛡️</div>
                <div class="metric-data">
                    <span class="metric-num">${totalAdminLogs}</span>
                    <span class="metric-title">Admin Operations Logged</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-icon-wrap icon-purple">👥</div>
                <div class="metric-data">
                    <span class="metric-num">${totalVoterLogs}</span>
                    <span class="metric-title">Voter Authentications</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-icon-wrap icon-emerald">🔒</div>
                <div class="metric-data">
                    <span class="metric-num" style="font-size: 1.25rem;">OWASP A09</span>
                    <span class="metric-title">Security Standard Compliant</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-icon-wrap icon-amber">⏱️</div>
                <div class="metric-data">
                    <span class="metric-num">${totalLogCount}</span>
                    <span class="metric-title">Total Unified Audit Logs</span>
                </div>
            </div>
        </section>

        <!-- Filter & Export Controls Bar -->
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.25rem;">
            <!-- Tab Navigation Buttons -->
            <div class="audit-tabs-nav" style="margin-bottom: 0;">
                <button type="button" class="audit-tab-btn active" id="tabBtnAdmin" onclick="switchAuditTab('admin')">
                    🛡️ Admin Operations & Logins <span class="tab-badge">${totalAdminLogs}</span>
                </button>
                <button type="button" class="audit-tab-btn" id="tabBtnVoter" onclick="switchAuditTab('voter')">
                    👥 Voter Authentication History <span class="tab-badge">${totalVoterLogs}</span>
                </button>
            </div>

            <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
                <input type="text" id="auditSearchInput" class="table-search-input" placeholder="🔍 Search action, IP, email, metadata..." style="width: 280px;">
                <form action="${pageContext.request.contextPath}/admin/audit" method="GET" style="display: flex; gap: 0.5rem; align-items: center;">
                    <label for="limitSelect" class="form-label" style="margin-bottom: 0; font-size: 0.85rem;">Display Limit:</label>
                    <select id="limitSelect" name="limit" class="form-control" style="width: auto; padding: 0.35rem 0.65rem; font-size: 0.85rem;" onchange="this.form.submit()">
                        <option value="50" ${currentLimit == 50 ? 'selected' : ''}>50</option>
                        <option value="100" ${currentLimit == 100 ? 'selected' : ''}>100</option>
                        <option value="200" ${currentLimit == 200 ? 'selected' : ''}>200</option>
                        <option value="500" ${currentLimit == 500 ? 'selected' : ''}>500</option>
                    </select>
                </form>
                <button type="button" onclick="window.print()" class="btn btn-sm btn-outline-secondary">
                    🖨️ Export View
                </button>
            </div>
        </div>

        <!-- ================================================================= -->
        <!-- TAB 1: Admin Operations & Logins Section                          -->
        <!-- ================================================================= -->
        <section id="auditPanelAdmin" class="admin-results-section" style="margin-bottom: 3rem;">
            <div class="election-tally-card">
                <div class="tally-card-header">
                    <div>
                        <h2 class="tally-election-title" style="font-size: 1.25rem;">Admin Operations & Governance Ledger</h2>
                        <p class="tally-election-desc">Administrative activities, election modifications, candidate nominations, and admin logins.</p>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="tally-table" id="adminAuditTable">
                        <thead>
                            <tr>
                                <th style="width: 70px;">ID</th>
                                <th style="width: 175px;">Timestamp</th>
                                <th style="width: 200px;">Administrator</th>
                                <th style="width: 180px;">Action Type</th>
                                <th style="width: 140px;">IP Address</th>
                                <th>Operation Metadata</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty adminLogs}">
                                    <c:forEach var="log" items="${adminLogs}">
                                        <tr class="audit-table-row">
                                            <td>
                                                <span class="rank-col">#${log.logId}</span>
                                            </td>
                                            <td style="font-family: var(--font-mono); font-size: 0.8rem; color: var(--text-muted);">
                                                ${log.createdAt}
                                            </td>
                                            <td>
                                                <strong>${log.adminEmail}</strong>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${log.actionType == 'ADMIN_LOGIN' || log.actionType == 'LOGIN'}">
                                                        <span class="badge badge-success">✓ ${log.actionType}</span>
                                                    </c:when>
                                                    <c:when test="${log.actionType == 'ADMIN_LOGIN_FAILED'}">
                                                        <span class="badge badge-danger">✗ ${log.actionType}</span>
                                                    </c:when>
                                                    <c:when test="${log.actionType == 'CREATE_ELECTION' || log.actionType == 'NOMINATE_CANDIDATE'}">
                                                        <span class="badge badge-primary">➕ ${log.actionType}</span>
                                                    </c:when>
                                                    <c:when test="${log.actionType == 'DELETE_ELECTION' || log.actionType == 'DELETE_CANDIDATE'}">
                                                        <span class="badge badge-danger">🗑️ ${log.actionType}</span>
                                                    </c:when>
                                                    <c:when test="${log.actionType == 'UPDATE_ELECTION_STATUS'}">
                                                        <span class="badge badge-secondary">⚙️ ${log.actionType}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-secondary">${log.actionType}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <code style="background: var(--accent-light); padding: 0.2rem 0.45rem; border-radius: 4px; font-size: 0.8rem; color: var(--accent-primary); border: 1px solid var(--border-color);">
                                                    ${log.ipAddress}
                                                </code>
                                            </td>
                                            <td style="font-size: 0.85rem; color: var(--text-muted);">
                                                <c:out value="${log.details}" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" class="text-center text-muted" style="padding: 2.5rem 1rem;">
                                            <div class="empty-state-card" style="margin: 0; box-shadow: none; border: none; background: transparent;">
                                                <div class="empty-icon">🛡️</div>
                                                <h3>No Administrative Records Found</h3>
                                                <p>Administrative activities will be automatically recorded here as they occur.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
                <div id="noAdminAuditMatches" class="filter-empty-state" style="display: none; padding: 2rem; text-align: center;">
                    <span style="font-size: 2rem; display: block; margin-bottom: 0.5rem;">🔍</span>
                    <h4 style="color: var(--text-primary); margin-bottom: 0.25rem;">No Matching Administrative Logs</h4>
                    <p style="color: var(--text-muted); font-size: 0.9rem; margin: 0;">No administrative operations match your search query.</p>
                </div>
            </div>
        </section>

        <!-- ================================================================= -->
        <!-- TAB 2: Voter Authentication History Section                       -->
        <!-- ================================================================= -->
        <section id="auditPanelVoter" class="admin-results-section" style="display: none; margin-bottom: 3rem;">
            <div class="election-tally-card">
                <div class="tally-card-header">
                    <div>
                        <h2 class="tally-election-title" style="font-size: 1.25rem;">Voter Authentication History Ledger</h2>
                        <p class="tally-election-desc">Real-time log of student and institutional voter logins, status verifications, and access attempts.</p>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="tally-table" id="voterAuditTable">
                        <thead>
                            <tr>
                                <th style="width: 70px;">ID</th>
                                <th style="width: 175px;">Timestamp</th>
                                <th style="width: 220px;">Voter Email</th>
                                <th style="width: 150px;">Authentication Status</th>
                                <th style="width: 140px;">IP Address</th>
                                <th>Authentication Metadata</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty voterLogs}">
                                    <c:forEach var="vlog" items="${voterLogs}">
                                        <tr class="audit-table-row">
                                            <td>
                                                <span class="rank-col">#${vlog.logId}</span>
                                            </td>
                                            <td style="font-family: var(--font-mono); font-size: 0.8rem; color: var(--text-muted);">
                                                ${vlog.createdAt}
                                            </td>
                                            <td>
                                                <strong>${vlog.adminEmail}</strong>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${vlog.actionType == 'VOTER_LOGIN'}">
                                                        <span class="badge badge-success">✓ SUCCESS</span>
                                                    </c:when>
                                                    <c:when test="${vlog.actionType == 'VOTER_LOGIN_FAILED'}">
                                                        <span class="badge badge-danger">✗ FAILED</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-secondary">${vlog.actionType}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <code style="background: var(--accent-light); padding: 0.2rem 0.45rem; border-radius: 4px; font-size: 0.8rem; color: var(--accent-primary); border: 1px solid var(--border-color);">
                                                    ${vlog.ipAddress}
                                                </code>
                                            </td>
                                            <td style="font-size: 0.85rem; color: var(--text-muted);">
                                                <c:out value="${vlog.details}" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="6" class="text-center text-muted" style="padding: 2.5rem 1rem;">
                                            <div class="empty-state-card" style="margin: 0; box-shadow: none; border: none; background: transparent;">
                                                <div class="empty-icon">👥</div>
                                                <h3>No Voter Authentication Records Yet</h3>
                                                <p>Student voter logins from the voter portal will be recorded here in real-time.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
                <div id="noVoterAuditMatches" class="filter-empty-state" style="display: none; padding: 2rem; text-align: center;">
                    <span style="font-size: 2rem; display: block; margin-bottom: 0.5rem;">🔍</span>
                    <h4 style="color: var(--text-primary); margin-bottom: 0.25rem;">No Matching Voter Authentication Logs</h4>
                    <p style="color: var(--text-muted); font-size: 0.9rem; margin: 0;">No voter authentication events match your search query.</p>
                </div>
            </div>
        </section>

    </main>

    <script>
        function switchAuditTab(tabName) {
            var adminPanel = document.getElementById("auditPanelAdmin");
            var voterPanel = document.getElementById("auditPanelVoter");
            var btnAdmin = document.getElementById("tabBtnAdmin");
            var btnVoter = document.getElementById("tabBtnVoter");

            if (tabName === "voter") {
                if (adminPanel) adminPanel.style.display = "none";
                if (voterPanel) voterPanel.style.display = "block";
                if (btnAdmin) btnAdmin.classList.remove("active");
                if (btnVoter) btnVoter.classList.add("active");
            } else {
                if (adminPanel) adminPanel.style.display = "block";
                if (voterPanel) voterPanel.style.display = "none";
                if (btnAdmin) btnAdmin.classList.add("active");
                if (btnVoter) btnVoter.classList.remove("active");
            }
        }
    </script>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
