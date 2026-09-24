<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- If accessed directly without going through AdminCandidateServlet, redirect --%>
<c:if test="${empty allElections}">
    <c:redirect url="/admin/candidates" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Candidate Registry - Online Voting System</title>
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
                <div class="banner-pill admin-pill">Candidate Provisioning</div>
                <h1 class="admin-banner-title">Candidate Directory & Nominations</h1>
                <p class="admin-banner-sub">Attach competing candidates, party symbols, and policy manifestos to elections.</p>
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

        <!-- Election Selection Bar -->
        <section class="arch-container" style="margin-bottom: 2rem; padding: 1.25rem 1.75rem;">
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <label for="electionSelect" class="form-label" style="margin-bottom: 0.25rem;">Select Target Election:</label>
                    <select id="electionSelect" class="form-control" style="width: auto; min-width: 320px; font-weight: 600;" 
                            onchange="window.location.href='${pageContext.request.contextPath}/admin/candidates?electionId=' + this.value">
                        <c:forEach var="el" items="${allElections}">
                            <option value="${el.electionId}" ${el.electionId == selectedElectionId ? 'selected' : ''}>
                                #${el.electionId}: ${el.title} (${el.status})
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <c:if test="${not empty selectedElection}">
                    <div class="election-meta-box">
                        <span class="badge ${selectedElection.status == 'ACTIVE' ? 'badge-success' : 'badge-secondary'}">
                            ${selectedElection.status}
                        </span>
                        <span class="text-muted small" style="margin-left: 0.5rem;">
                            Closes: <strong>${selectedElection.endDate}</strong>
                        </span>
                    </div>
                </c:if>
            </div>
        </section>

        <c:choose>
            <c:when test="${not empty selectedElection}">
                <!-- Add Candidate Form -->
                <section class="arch-container" style="margin-bottom: 2.5rem;">
                    <h2 class="section-title">
                        <span>➕</span> Nominate New Candidate for "${selectedElection.title}"
                    </h2>

                    <form action="${pageContext.request.contextPath}/admin/candidates" method="POST" class="auth-form" 
                          style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.25rem;">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="electionId" value="${selectedElection.electionId}">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">

                        <div class="form-group">
                            <label class="form-label" for="candidateName">Candidate Full Name *</label>
                            <input type="text" id="candidateName" name="name" class="form-control" 
                                   placeholder="e.g. Samantha Reed" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="partySymbol">Party Affiliation & Symbol *</label>
                            <input type="text" id="partySymbol" name="partySymbol" class="form-control" 
                                   placeholder="e.g. Progressive Tech Alliance (💻)" required>
                        </div>

                        <div class="form-group" style="grid-column: 1 / -1;">
                            <label class="form-label" for="manifesto">Policy Manifesto / Campaign Statement</label>
                            <textarea id="manifesto" name="manifesto" class="form-control" rows="3" 
                                      placeholder="Pledges, campaign agenda, key student issues addressed..."></textarea>
                        </div>

                        <div class="form-group" style="grid-column: 1 / -1;">
                            <button type="submit" class="btn btn-primary btn-lg" style="align-self: flex-start;">
                                Register Candidate Nomination &rarr;
                            </button>
                        </div>
                    </form>
                </section>

                <!-- Currently Nominated Candidates -->
                <section class="admin-results-section">
                    <h2 class="section-title">
                        <span>👥</span> Competing Candidates (${not empty candidates ? candidates.size() : 0})
                    </h2>

                    <c:choose>
                        <c:when test="${not empty candidates}">
                            <div class="candidate-cards-grid">
                                <c:forEach var="cand" items="${candidates}">
                                    <div class="portal-card" style="position: relative;">
                                        <div class="candidate-card-top" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                                            <span class="candidate-symbol-badge" style="font-size: 1.3rem;">${cand.partySymbol}</span>
                                            <form action="${pageContext.request.contextPath}/admin/candidates" method="POST" 
                                                  onsubmit="return confirm('Remove candidate ${cand.name} from this election?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="candidateId" value="${cand.candidateId}">
                                                <input type="hidden" name="electionId" value="${selectedElection.electionId}">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger">Remove</button>
                                            </form>
                                        </div>

                                        <h3 class="candidate-name" style="font-size: 1.2rem; font-weight: 800; margin-bottom: 0.25rem;">
                                            ${cand.name}
                                        </h3>
                                        <div class="candidate-party" style="font-size: 0.85rem; color: var(--primary); font-weight: 600; margin-bottom: 0.85rem;">
                                            ${cand.partySymbol}
                                        </div>

                                        <div class="candidate-manifesto" style="font-size: 0.85rem; color: var(--text-secondary); background: #f8fafc; padding: 0.85rem; border-radius: var(--radius-sm); border-left: 3px solid var(--primary); margin-top: auto;">
                                            <strong>Manifesto:</strong> ${cand.manifesto}
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state-card">
                                <div class="empty-icon">👥</div>
                                <h3>No Candidates Registered</h3>
                                <p>No candidates have been nominated for this election yet. Fill out the form above to add nominees.</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>
            <c:otherwise>
                <div class="empty-state-card">
                    <div class="empty-icon">🗳️</div>
                    <h3>No Elections Available</h3>
                    <p>Please create an election first before registering candidates.</p>
                    <a href="${pageContext.request.contextPath}/admin/elections" class="btn btn-primary" style="margin-top: 1rem;">
                        Create Election
                    </a>
                </div>
            </c:otherwise>
        </c:choose>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
