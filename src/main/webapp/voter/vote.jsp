<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- Mandatory Voter Profile Verification Gatekeeper --%>
<c:if test="${not empty sessionScope.currentUser && !sessionScope.currentUser.profileComplete}">
    <c:redirect url="/voter/profile?warning=Please+complete+your+official+student+profile+before+participating+in+any+elections." />
</c:if>

<%-- Defensive redirect if election attribute is missing --%>
<c:if test="${empty election}">
    <c:choose>
        <c:when test="${not empty param.electionId}">
            <c:redirect url="/voter/vote?electionId=${param.electionId}" />
        </c:when>
        <c:otherwise>
            <c:redirect url="/voter/dashboard" />
        </c:otherwise>
    </c:choose>
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
            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <div class="booth-badge">Official Secret Ballot</div>
                    <h1 class="booth-election-title">${election.title}</h1>
                    <p class="booth-election-desc">${election.description}</p>
                </div>
                <!-- Dynamic Polling Countdown Timer -->
                <div id="countdownContainer" data-end-time="${not empty election.endTimeMillis ? election.endTimeMillis : 0}">
                    <span class="countdown-badge" id="pollingCountdownBadge">
                        ⏱️ Time Remaining: <strong id="countdownTimer">Loading...</strong>
                    </span>
                </div>
            </div>

            <!-- Election Closed Banner (Triggered when countdown reaches 0) -->
            <div id="electionClosedBanner" class="election-closed-banner" style="display: none; margin-top: 1rem;">
                <span>⚠️</span> <strong>Election Closed:</strong> The voting window for this election has ended. No further ballots may be committed.
            </div>

            <div class="booth-notice" style="margin-top: 1rem;">
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
                                    <div style="margin-top: 0.85rem;">
                                        <button type="button" class="btn btn-manifesto" 
                                                onclick="event.stopPropagation(); event.preventDefault(); openManifestoModal('<c:out value="${candidate.name}"/>', '<c:out value="${candidate.partySymbol}"/>', '<c:out value="${candidate.manifesto}"/>')">
                                            📖 Read Full Manifesto
                                        </button>
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

        <!-- Candidate Manifesto Modal Dialog (Dual-Theme Styled) -->
        <div id="manifestoModal" class="manifesto-modal-overlay" style="display: none;">
            <div class="manifesto-modal-card" role="dialog" aria-modal="true" aria-labelledby="manifestoCandidateTitle">
                <div class="manifesto-modal-header">
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <span id="manifestoSymbolBadge" style="font-size: 1.8rem;">🎓</span>
                        <div>
                            <h3 id="manifestoCandidateTitle" style="font-size: 1.35rem; font-weight: 800; margin: 0; color: var(--text-primary);">Candidate Agenda</h3>
                            <span style="font-size: 0.8rem; color: var(--accent-primary); font-weight: 700; text-transform: uppercase;">Official Policy Statement</span>
                        </div>
                    </div>
                    <button type="button" class="manifesto-modal-close" onclick="closeManifestoModal()" aria-label="Close manifesto">&times;</button>
                </div>
                <div class="manifesto-modal-body" style="line-height: 1.7; font-size: 0.95rem; color: var(--text-primary);">
                    <p id="manifestoBodyText" style="white-space: pre-wrap; margin: 0;"></p>
                </div>
                <div style="margin-top: 1.75rem; padding-top: 1rem; border-top: 1px solid var(--border-color); display: flex; justify-content: flex-end;">
                    <button type="button" class="btn btn-outline-secondary" onclick="closeManifestoModal()">Close Manifesto</button>
                </div>
            </div>
        </div>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

    <script>
    function openManifestoModal(name, symbol, manifesto) {
        document.getElementById('manifestoCandidateTitle').textContent = name + " - Manifesto";
        document.getElementById('manifestoSymbolBadge').textContent = symbol || '🗳️';
        document.getElementById('manifestoBodyText').textContent = manifesto || "No manifesto statement submitted by candidate.";
        document.getElementById('manifestoModal').style.display = 'flex';
    }

    function closeManifestoModal() {
        document.getElementById('manifestoModal').style.display = 'none';
    }

    document.addEventListener("DOMContentLoaded", function () {
        var mModal = document.getElementById('manifestoModal');
        if (mModal) {
            mModal.addEventListener('click', function(e) {
                if (e.target === mModal) closeManifestoModal();
            });
        }

        // Real-Time Dynamic Polling Countdown
        var countdownContainer = document.getElementById("countdownContainer");
        var timerEl = document.getElementById("countdownTimer");
        var badgeEl = document.getElementById("pollingCountdownBadge");
        var closedBanner = document.getElementById("electionClosedBanner");
        var openConfirmBtn = document.getElementById("openConfirmModalBtn");

        if (countdownContainer && timerEl) {
            var rawEndTime = parseInt(countdownContainer.getAttribute("data-end-time"), 10);
            var endTime = (rawEndTime && !isNaN(rawEndTime) && rawEndTime > 0) ? rawEndTime : (Date.now() + 7200000);

            function updateCountdown() {
                var now = Date.now();
                var remaining = endTime - now;

                if (remaining <= 0) {
                    timerEl.textContent = "00:00:00 (POLLS CLOSED)";
                    if (badgeEl) badgeEl.classList.add("expired");
                    if (closedBanner) closedBanner.style.display = "flex";
                    if (openConfirmBtn) {
                        openConfirmBtn.disabled = true;
                        openConfirmBtn.textContent = "Polling Concluded";
                        openConfirmBtn.style.opacity = "0.6";
                    }
                    return;
                }

                var hours = Math.floor(remaining / (1000 * 60 * 60));
                var minutes = Math.floor((remaining % (1000 * 60 * 60)) / (1000 * 60));
                var seconds = Math.floor((remaining % (1000 * 60)) / 1000);

                var hh = (hours < 10 ? "0" : "") + hours;
                var mm = (minutes < 10 ? "0" : "") + minutes;
                var ss = (seconds < 10 ? "0" : "") + seconds;

                timerEl.textContent = hh + ":" + mm + ":" + ss;
            }

            updateCountdown();
            setInterval(updateCountdown, 1000);
        }
    });
    </script>
</body>
</html>
