<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Independent Ballot Verification - Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Include Reusable Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container page-container" style="max-width: 900px; padding: 2.5rem 1rem;">

        <!-- Header Hero Banner -->
        <section class="banner" style="text-align: center; margin-bottom: 2.5rem; background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%); color: #ffffff; border-radius: var(--radius-xl); padding: 3rem 2rem; border: 1px solid #312e81; box-shadow: var(--shadow-lg);">
            <div class="banner-pill" style="background: rgba(99, 102, 241, 0.2); color: #818cf8; border: 1px solid rgba(99, 102, 241, 0.4); display: inline-block; padding: 0.35rem 0.85rem; border-radius: 9999px; font-size: 0.8rem; font-weight: 700; margin-bottom: 1rem;">
                Public Cryptographic Audit
            </div>
            <h1 style="font-size: 2.2rem; font-weight: 900; margin-bottom: 1rem; color: #f8fafc; letter-spacing: -0.025em;">
                Independent Ballot Verification
            </h1>
            <p style="font-size: 1.05rem; color: #cbd5e1; max-width: 680px; margin: 0 auto; line-height: 1.6;">
                Verify that your secret ballot was recorded into the electoral ledger without compromising your anonymity or revealing your candidate choice.
            </p>
        </section>

        <!-- Receipt Lookup Form -->
        <section class="portal-card" style="background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: 2rem; box-shadow: var(--shadow-md); margin-bottom: 2.5rem;">
            <h2 style="font-size: 1.25rem; font-weight: 800; margin-bottom: 0.75rem; color: var(--text-primary); display: flex; align-items: center; gap: 0.5rem;">
                <span>🔍</span> Enter 64-Character SHA-256 Receipt Token
            </h2>
            <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.5rem;">
                Paste the digital receipt token you received upon casting your ballot:
            </p>

            <form action="${pageContext.request.contextPath}/verify-receipt" method="GET" style="display: flex; flex-direction: column; gap: 1rem;">
                <div>
                    <input type="text" name="receiptToken" class="form-control" 
                           value="<c:out value='${receiptToken}' />"
                           placeholder="e.g. 0ffe2a845b41dc4368f839e12cc58e5370865460e2103747592f12805da8b402" 
                           style="font-family: var(--font-mono); font-size: 0.88rem; padding: 0.85rem 1rem; border: 2px solid var(--border); border-radius: var(--radius-md); width: 100%; letter-spacing: 0.5px;"
                           required autofocus maxlength="64">
                </div>
                <div style="display: flex; gap: 0.75rem; flex-wrap: wrap; align-items: center;">
                    <button type="submit" class="btn btn-primary btn-lg" style="flex: 1; min-width: 200px;">
                        Verify Ballot Inclusion &rarr;
                    </button>
                    <c:if test="${searched}">
                        <a href="${pageContext.request.contextPath}/verify-receipt" class="btn btn-outline-secondary btn-lg">
                            Clear
                        </a>
                    </c:if>
                </div>
            </form>
        </section>

        <!-- Verification Results Outcome -->
        <c:if test="${searched}">
            <c:choose>
                <c:when test="${verified}">
                    <!-- Successful Proof Card -->
                    <section class="portal-card" style="border: 2px solid #10b981; background: #064e3b15; border-radius: var(--radius-xl); padding: 2.25rem; margin-bottom: 2.5rem;">
                        <div style="display: flex; align-items: flex-start; gap: 1rem; margin-bottom: 1.5rem;">
                            <div style="font-size: 2.5rem; line-height: 1;">✅</div>
                            <div>
                                <span class="badge badge-success" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;">
                                    Cryptographically Validated
                                </span>
                                <h3 style="font-size: 1.5rem; font-weight: 800; color: #065f46; margin: 0.5rem 0 0.25rem 0;">
                                    Ballot Successfully Verified in Official Ledger
                                </h3>
                                <p style="color: #047857; font-size: 0.95rem; margin: 0;">
                                    An authentic digital ballot matching your receipt was committed and permanently recorded.
                                </p>
                            </div>
                        </div>

                        <div style="background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); padding: 1.5rem; margin-bottom: 1.5rem;">
                            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem;">
                                <div>
                                    <div style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-secondary); margin-bottom: 0.25rem;">
                                        Election Event
                                    </div>
                                    <div style="font-size: 1.05rem; font-weight: 800; color: var(--text-primary);">
                                        <c:out value="${electionTitle}" />
                                    </div>
                                </div>

                                <div>
                                    <div style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-secondary); margin-bottom: 0.25rem;">
                                        Ledger Commit Timestamp
                                    </div>
                                    <div style="font-size: 1.05rem; font-weight: 700; color: var(--text-primary); font-family: var(--font-mono);">
                                        <c:out value="${voteTimestamp}" />
                                    </div>
                                </div>

                                <div>
                                    <div style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-secondary); margin-bottom: 0.25rem;">
                                        Anonymized Proof Index
                                    </div>
                                    <div style="font-size: 1.05rem; font-weight: 700; color: var(--primary);">
                                        Ballot ID #${ballotId}
                                    </div>
                                </div>
                            </div>

                            <hr style="margin: 1.25rem 0; border: none; border-top: 1px solid var(--border);">

                            <div>
                                <div style="font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: var(--text-secondary); margin-bottom: 0.35rem;">
                                    Immutable Verification Hash (SHA-256)
                                </div>
                                <code style="display: block; word-break: break-all; font-family: var(--font-mono); font-size: 0.85rem; background: #0f172a; color: #38bdf8; padding: 0.85rem 1rem; border-radius: var(--radius-sm); border: 1px solid #1e293b;">
                                    <c:out value="${receiptToken}" />
                                </code>
                            </div>
                        </div>

                        <!-- Secret Ballot Privacy Disclaimer -->
                        <div style="display: flex; gap: 0.75rem; align-items: flex-start; background: #ffffff; border: 1px solid rgba(16, 185, 129, 0.3); border-radius: var(--radius-md); padding: 1rem 1.25rem;">
                            <span style="font-size: 1.25rem;">🔒</span>
                            <div style="font-size: 0.85rem; color: #1e293b; line-height: 1.5;">
                                <strong>Zero-Knowledge Choice Secrecy Guaranteed:</strong>
                                By architectural design, your personal voter ID and specific candidate selection are decoupled from this receipt. This mathematically prevents vote-selling, coercion, and retaliation while providing complete assurance that your vote was counted.
                            </div>
                        </div>
                    </section>
                </c:when>

                <c:otherwise>
                    <!-- Failed Proof Card -->
                    <section class="portal-card" style="border: 2px solid #ef4444; background: #7f1d1d15; border-radius: var(--radius-xl); padding: 2rem; margin-bottom: 2.5rem;">
                        <div style="display: flex; align-items: flex-start; gap: 1rem;">
                            <div style="font-size: 2.25rem; line-height: 1;">❌</div>
                            <div>
                                <span class="badge badge-danger" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;">
                                    Verification Failed
                                </span>
                                <h3 style="font-size: 1.35rem; font-weight: 800; color: #991b1b; margin: 0.5rem 0 0.5rem 0;">
                                    No Record Found
                                </h3>
                                <p style="color: #b91c1c; font-size: 0.95rem; margin-bottom: 1rem;">
                                    <c:out value="${errorMessage}" />
                                </p>
                                <p style="color: var(--text-secondary); font-size: 0.85rem; margin: 0;">
                                    Please ensure that you copied all 64 characters of your digital receipt without any extra spaces or missing characters.
                                </p>
                            </div>
                        </div>
                    </section>
                </c:otherwise>
            </c:choose>
        </c:if>

        <!-- Educational Information Grid -->
        <section style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.5rem; margin-top: 1rem;">
            <div class="portal-card" style="padding: 1.75rem;">
                <div style="font-size: 1.8rem; margin-bottom: 0.75rem;">🛡️</div>
                <h4 style="font-size: 1.1rem; font-weight: 800; margin-bottom: 0.5rem;">
                    How Independent Auditing Works
                </h4>
                <p style="font-size: 0.88rem; color: var(--text-secondary); line-height: 1.6; margin: 0;">
                    When you cast a ballot, the transactional engine generates a unique 64-character SHA-256 cryptographic receipt token bound to your ballot timestamp and election nonce.
                </p>
            </div>

            <div class="portal-card" style="padding: 1.75rem;">
                <div style="font-size: 1.8rem; margin-bottom: 0.75rem;">🗳️</div>
                <h4 style="font-size: 1.1rem; font-weight: 800; margin-bottom: 0.5rem;">
                    Decoupled Secret Ballot Privacy
                </h4>
                <p style="font-size: 0.88rem; color: var(--text-secondary); line-height: 1.6; margin: 0;">
                    Your vote choice is recorded into the anonymous ballot box without attaching your student ID or name. Only participation status is tracked, preserving absolute voting confidentiality.
                </p>
            </div>
        </section>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
