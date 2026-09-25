<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- If election attribute is missing, redirect through VoteBoothServlet to load data --%>
<c:if test="${empty election && not empty param.electionId}">
    <c:redirect url="/voter/vote?electionId=${param.electionId}" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Voting Booth - ${election.title} | CampusVote</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="booth-page">

    <!-- Include Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container booth-container">

        <!-- Booth Header -->
        <div class="booth-header-card">
            <div class="booth-badge">Official Secret Ballot</div>
            <h1 class="booth-election-title">${election.title}</h1>
            <p class="booth-election-desc">${election.description}</p>
            <div class="booth-notice">
                <span class="notice-icon">🔒</span>
                <span>
                    <strong>Secret Ballot Protected:</strong> Your selection is stored anonymously. Your identity is separated from this ballot box.
                </span>
            </div>
        </div>

        <!-- Ballot Form -->
        <form id="voteForm" action="${pageContext.request.contextPath}/voter/cast-vote" method="POST" class="ballot-form">
            <input type="hidden" name="electionId" value="${election.electionId}">
            <input type="hidden" name="csrfToken" value="${csrfToken}">

            <div class="candidates-heading">
                <h2>Select One Candidate</h2>
                <p>Click on a candidate card to mark your ballot choice:</p>
            </div>

            <!-- Candidate Cards Selection Grid -->
            <div class="candidate-cards-grid">
                <c:choose>
                    <c:when test="${not empty candidates}">
                        <c:forEach var="candidate" items="${candidates}">
                            <label class="candidate-choice-card" for="candidate_${candidate.candidateId}">
                                <div class="candidate-card-top">
                                    <div class="candidate-radio-wrap">
                                        <input type="radio" id="candidate_${candidate.candidateId}" 
                                               name="candidateId" value="${candidate.candidateId}" 
                                               data-candidate-name="${candidate.name}" 
                                               data-candidate-symbol="${candidate.partySymbol}" 
                                               required class="candidate-radio">
                                        <span class="custom-radio"></span>
                                    </div>
                                    <div class="candidate-symbol-badge">
                                        ${candidate.partySymbol}
                                    </div>
                                </div>

                                <div class="candidate-card-body">
                                    <h3 class="candidate-name">${candidate.name}</h3>
                                    <div class="candidate-party">${candidate.partySymbol}</div>
                                    <div class="candidate-manifesto">
                                        <p><strong>Manifesto:</strong> ${candidate.manifesto}</p>
                                    </div>
                                </div>
                            </label>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-warning">
                            No candidate nominations have been registered for this election.
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Ballot Submit Action Bar -->
            <div class="ballot-action-bar">
                <a href="${pageContext.request.contextPath}/voter/dashboard" class="btn btn-outline-secondary">
                    &larr; Return to Dashboard
                </a>
                <button type="button" id="openConfirmModalBtn" class="btn btn-primary btn-lg" disabled>
                    Review & Cast Ballot &rarr;
                </button>
            </div>
        </form>

        <!-- Vote Confirmation Modal Dialog -->
        <div id="voteConfirmModal" class="modal-overlay" style="display: none;">
            <div class="modal-card">
                <div class="modal-header">
                    <div class="modal-icon-badge">🗳️</div>
                    <h3 class="modal-title">Confirm Your Ballot</h3>
                </div>

                <div class="modal-body">
                    <p class="modal-warning">
                        Please review your selection carefully. Under democratic voting rules, 
                        <strong>you can only vote once per election and your vote cannot be changed once submitted</strong>.
                    </p>

                    <div class="selected-candidate-preview">
                        <span class="preview-label">Selected Candidate:</span>
                        <div class="preview-box">
                            <span id="modalCandidateSymbol" class="preview-symbol"></span>
                            <span id="modalCandidateName" class="preview-name">No Candidate Selected</span>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" id="closeConfirmModalBtn" class="btn btn-outline-secondary">
                        Change Selection
                    </button>
                    <button type="button" id="submitFinalVoteBtn" class="btn btn-success btn-lg">
                        Confirm & Cast Ballot
                    </button>
                </div>
            </div>
        </div>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
