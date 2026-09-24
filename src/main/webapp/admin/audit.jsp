<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- If accessed directly without going through AdminAuditServlet, redirect --%>
<c:if test="${empty auditLogs && empty totalLogCount}">
    <c:redirect url="/admin/audit" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Security Audit Trail - Online Voting System</title>
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
                <p class="admin-banner-sub">Immutable, timestamped record of administrative interventions, provisioning, and authentication events.</p>
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
                <div class="metric-icon-wrap icon-blue">📜</div>
                <div class="metric-data">
                    <span class="metric-num">${totalLogCount}</span>
                    <span class="metric-title">Total Operations Logged</span>
                </div>
            </div>

            <div class="metric-card">
                <div class="metric-icon-wrap icon-purple">🛡️</div>
                <div class="metric-data">
                    <span class="metric-num">${auditLogs.size()}</span>
                    <span class="metric-title">Showing Recent Records</span>
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
                    <span class="metric-num" style="font-size: 1.1rem;">Append-Only</span>
                    <span class="metric-title">Storage Integrity Model</span>
                </div>
            </div>
        </section>

        <!-- Audit Records Table Section -->
        <section class="admin-results-section" style="margin-bottom: 3rem;">
            <div class="election-tally-card">
                <div class="tally-card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <h2 class="tally-election-title" style="font-size: 1.25rem;">Administrative Governance Ledger</h2>
                        <p class="tally-election-desc">Real-time system telemetry and administrator accountability log.</p>
                    </div>
                    <div style="display: flex; gap: 0.75rem; align-items: center;">
                        <form action="${pageContext.request.contextPath}/admin/audit" method="GET" style="display: flex; gap: 0.5rem; align-items: center;">
                            <label for="limitSelect" class="form-label" style="margin-bottom: 0; font-size: 0.85rem;">Records:</label>
                            <select id="limitSelect" name="limit" class="form-control" style="width: auto; padding: 0.35rem 0.65rem; font-size: 0.85rem;" onchange="this.form.submit()">
                                <option value="50" ${currentLimit == 50 ? 'selected' : ''}>50</option>
                                <option value="100" ${currentLimit == 100 ? 'selected' : ''}>100</option>
                                <option value="200" ${currentLimit == 200 ? 'selected' : ''}>200</option>
                                <option value="500" ${currentLimit == 500 ? 'selected' : ''}>500</option>
                            </select>
                        </form>
                        <button type="button" onclick="window.print()" class="btn btn-sm btn-outline-secondary">
                            🖨️ Export Log
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="tally-table">
                        <thead>
                            <tr>
                                <th style="width: 70px;">ID</th>
                                <th style="width: 175px;">Timestamp</th>
                                <th style="width: 200px;">Administrator</th>
                                <th style="width: 170px;">Action Type</th>
                                <th style="width: 140px;">IP Address</th>
                                <th>Operation Metadata</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty auditLogs}">
                                    <c:forEach var="log" items="${auditLogs}">
                                        <tr>
                                            <td>
                                                <span class="rank-col">#${log.logId}</span>
                                            </td>
                                            <td style="font-family: var(--font-mono); font-size: 0.8rem; color: #94a3b8;">
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
                                                <code style="background: #020617; padding: 0.2rem 0.45rem; border-radius: 4px; font-size: 0.8rem; color: #38bdf8; border: 1px solid #1e293b;">
                                                    ${log.ipAddress}
                                                </code>
                                            </td>
                                            <td style="font-size: 0.85rem; color: #cbd5e1;">
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
                                                <h3>No Audit Records Logged Yet</h3>
                                                <p>Administrative actions such as election provisioning and status toggles will be recorded here.</p>
                                            </div>
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
