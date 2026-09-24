<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- If accessed directly without going through AdminElectionServlet, redirect --%>
<c:if test="${empty elections}">
    <c:redirect url="/admin/elections" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Elections - Online Voting System</title>
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
                <div class="banner-pill admin-pill">Election Management</div>
                <h1 class="admin-banner-title">Schedule & Manage Elections</h1>
                <p class="admin-banner-sub">Configure democratic polling windows, toggle statuses, and oversee electoral events.</p>
            </div>
        </section>

        <!-- Alerts -->
        <c:if test="${not empty param.success}">
            <div class="alert alert-success" role="alert">
                <span class="alert-icon">✓</span>
                <div class="alert-content"><c:out value="${param.success}" /></div>
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger" role="alert">
                <span class="alert-icon">⚠️</span>
                <div class="alert-content"><c:out value="${param.error}" /></div>
            </div>
        </c:if>

        <!-- Create New Election Card -->
        <section class="arch-container" style="margin-bottom: 2.5rem;">
            <h2 class="section-title">
                <span>➕</span> Provision New Election
            </h2>
            <form action="${pageContext.request.contextPath}/admin/elections" method="POST" class="auth-form" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.25rem;">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="csrfToken" value="${csrfToken}">

                <div class="form-group" style="grid-column: 1 / -1;">
                    <label class="form-label" for="electionTitle">Election Title *</label>
                    <input type="text" id="electionTitle" name="title" class="form-control" 
                           placeholder="e.g. Student Council Executive Board Election 2026" required>
                </div>

                <div class="form-group" style="grid-column: 1 / -1;">
                    <label class="form-label" for="electionDesc">Description</label>
                    <textarea id="electionDesc" name="description" class="form-control" rows="2" 
                              placeholder="Describe the scope, voter eligibility, and positions being contested..."></textarea>
                </div>

                <div class="form-group">
                    <label class="form-label" for="startDate">Start Date & Time *</label>
                    <input type="datetime-local" id="startDate" name="startDate" class="form-control" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="endDate">End Date & Time *</label>
                    <input type="datetime-local" id="endDate" name="endDate" class="form-control" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="status">Initial Status</label>
                    <select id="status" name="status" class="form-control" style="padding-left: 1rem;">
                        <option value="ACTIVE" selected>ACTIVE (Open for Immediate Voting)</option>
                        <option value="SCHEDULED">SCHEDULED (Upcoming Window)</option>
                        <option value="DRAFT">DRAFT (Under Configuration)</option>
                    </select>
                </div>

                <div class="form-group" style="display: flex; align-items: flex-end;">
                    <button type="submit" class="btn btn-primary btn-block btn-lg" style="height: 48px;">
                        Create Election &rarr;
                    </button>
                </div>
            </form>
        </section>

        <!-- Existing Elections Table -->
        <section class="admin-results-section">
            <h2 class="section-title">
                <span>📋</span> All Scheduled Elections (${not empty elections ? elections.size() : 0})
            </h2>

            <div class="table-responsive" style="background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); box-shadow: var(--shadow-sm);">
                <table class="tally-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Election Details</th>
                            <th>Status</th>
                            <th>Polling Timeline</th>
                            <th>Quick Status Toggle</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="el" items="${elections}">
                            <tr>
                                <td><strong>#${el.electionId}</strong></td>
                                <td>
                                    <strong>${el.title}</strong>
                                    <div class="text-muted small">${el.description}</div>
                                </td>
                                <td>
                                    <span class="badge ${el.status == 'ACTIVE' ? 'badge-success' : (el.status == 'CLOSED' ? 'badge-danger' : 'badge-secondary')}">
                                        ${el.status}
                                    </span>
                                </td>
                                <td class="small">
                                    <div><strong>Starts:</strong> ${el.startDate}</div>
                                    <div><strong>Ends:</strong> ${el.endDate}</div>
                                </td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/admin/elections" method="POST" style="display: flex; gap: 0.35rem; align-items: center;">
                                        <input type="hidden" name="action" value="updateStatus">
                                        <input type="hidden" name="electionId" value="${el.electionId}">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <select name="status" class="form-control" style="font-size: 0.8rem; padding: 0.3rem 0.6rem; width: auto;">
                                            <option value="ACTIVE" ${el.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                            <option value="CLOSED" ${el.status == 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                                            <option value="SCHEDULED" ${el.status == 'SCHEDULED' ? 'selected' : ''}>SCHEDULED</option>
                                            <option value="PUBLISHED" ${el.status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                                        </select>
                                        <button type="submit" class="btn btn-sm btn-outline-primary">Update</button>
                                    </form>
                                </td>
                                <td>
                                    <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
                                        <a href="${pageContext.request.contextPath}/admin/candidates?electionId=${el.electionId}" class="btn btn-sm btn-outline-secondary">
                                            Candidates
                                        </a>
                                        <a href="${pageContext.request.contextPath}/admin/results?electionId=${el.electionId}" class="btn btn-sm btn-purple">
                                            Results
                                        </a>
                                        <form action="${pageContext.request.contextPath}/admin/elections" method="POST" onsubmit="return confirm('Are you sure you want to delete Election #${el.electionId}? All associated votes will be permanently removed.');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="electionId" value="${el.electionId}">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

    <script>
        // Set default datetime values for convenience
        document.addEventListener("DOMContentLoaded", function() {
            const startInput = document.getElementById("startDate");
            const endInput = document.getElementById("endDate");
            if (startInput && !startInput.value) {
                const now = new Date();
                startInput.value = now.toISOString().slice(0, 16);
            }
            if (endInput && !endInput.value) {
                const future = new Date();
                future.setDate(future.getDate() + 7);
                endInput.value = future.toISOString().slice(0, 16);
            }
        });
    </script>
</body>
</html>
