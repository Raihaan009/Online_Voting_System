<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<%-- If accessed directly without going through the servlet, redirect to populate model attributes --%>
<c:if test="${empty activeElections && empty votingStatusMap}">
    <c:redirect url="/voter/dashboard" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Voter Dashboard - CampusVote | Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="dashboard-page">

    <!-- Include Reusable Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container dashboard-container">

        <!-- Welcome Banner -->
        <section class="dashboard-banner">
            <div class="banner-content">
                <div class="banner-pill">Authenticated Voter Portal</div>
                <h1 class="banner-title">Welcome back, <span class="gradient-text">${sessionScope.currentUser.name}</span></h1>
                <p class="banner-subtitle">
                    Institutional Email: <strong>${sessionScope.currentUser.email}</strong> &bull; 
                    Voter Status: <span class="badge badge-success">${sessionScope.currentUser.status}</span>
                </p>
            </div>
            <div class="banner-meta">
                <div class="meta-item">
                    <span class="meta-label">Active Elections</span>
                    <span class="meta-val">${not empty activeElections ? activeElections.size() : 0}</span>
                </div>
            </div>
        </section>

        <!-- Notification Alerts -->
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger" role="alert">
                <span class="alert-icon">⚠️</span>
                <div class="alert-content">
                    <c:choose>
                        <c:when test="${param.error == 'already_voted' || param.error == 'duplicate_vote'}">
                            Duplicate vote blocked: You have already cast your ballot for this election. Each eligible voter is permitted exactly one vote.
                        </c:when>
                        <c:otherwise>
                            <c:out value="${param.error}" />
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:if>

        <c:if test="${not empty param.success}">
            <div class="alert alert-success" role="alert">
                <span class="alert-icon">✓</span>
                <div class="alert-content"><c:out value="${param.success}" /></div>
            </div>
        </c:if>

        <!-- Active Elections Section -->
        <section class="elections-section">
            <div class="section-heading-wrap">
                <div>
                    <h2 class="section-heading">Active Democratic Elections</h2>
                    <p class="section-subheading">Below are the currently scheduled polling events open for voting.</p>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty activeElections}">
                    <div class="elections-grid">
                        <c:forEach var="election" items="${activeElections}">
                            <c:set var="hasVotedThisElection" value="${votingStatusMap[election.electionId]}" />

                            <div class="election-card ${hasVotedThisElection ? 'election-card-voted' : ''}">
                                <div class="election-card-header">
                                    <div class="election-title-group">
                                        <h3 class="election-title">${election.title}</h3>
                                        <span class="status-pill status-active">● Active Polling</span>
                                    </div>
                                    <p class="election-desc">${election.description}</p>
                                </div>

                                <div class="election-window-info">
                                    <span class="window-item">
                                        <span class="icon">📅</span>
                                        <span>Closes: <strong>${election.endDate}</strong></span>
                                    </span>
                                </div>

                                <!-- Competing Candidates List Preview -->
                                <div class="candidates-preview">
                                    <h4 class="candidates-preview-title">Competing Candidates:</h4>
                                    <c:set var="candidates" value="${candidatesMap[election.electionId]}" />
                                    <c:choose>
                                        <c:when test="${not empty candidates}">
                                            <div class="candidate-pill-list">
                                                <c:forEach var="candidate" items="${candidates}">
                                                    <div class="candidate-pill">
                                                        <span class="candidate-pill-symbol">${candidate.partySymbol}</span>
                                                        <span class="candidate-pill-name">${candidate.name}</span>
                                                    </div>
                                                </c:forEach>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="text-muted small">No candidate nominations registered yet.</p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- Action Footer with Dynamic Badges -->
                                <div class="election-card-footer">
                                    <c:choose>
                                        <c:when test="${hasVotedThisElection}">
                                            <div class="voted-badge-box">
                                                <span class="voted-icon">✓</span>
                                                <div class="voted-text">
                                                    <strong>Ballot Submitted</strong>
                                                    <small>Your vote is recorded in the secret ballot box.</small>
                                                </div>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/voter/vote?electionId=${election.electionId}" 
                                               class="btn btn-primary btn-block">
                                                🗳️ Vote Now &rarr;
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>

                <c:otherwise>
                    <div class="empty-state-card">
                        <div class="empty-icon">🗳️</div>
                        <h3>No Active Elections</h3>
                        <p>There are no polling events currently open for voting. Please check back later or contact the Election Commission.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
