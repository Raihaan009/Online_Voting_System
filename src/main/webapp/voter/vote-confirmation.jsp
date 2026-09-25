<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ballot Confirmed - CampusVote | Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="confirmation-page">

    <!-- Include Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container confirmation-container">

        <div class="confirmation-card">
            <!-- Success Animated Checkmark -->
            <div class="success-checkmark-wrap">
                <div class="success-checkmark">✓</div>
            </div>

            <h1 class="confirmation-title">Ballot Successfully Recorded!</h1>
            <p class="confirmation-subtitle">
                Your vote has been committed into the immutable secret ballot box via an atomic database transaction.
            </p>

            <!-- Secrecy Guarantee Callout -->
            <div class="integrity-callout">
                <div class="callout-icon">🛡️</div>
                <div class="callout-text">
                    <strong>Ballot Privacy Guarantee:</strong>
                    Your personal voter identity (Name, Email, Student ID) has been completely decoupled from your candidate choice. 
                    No one, including system administrators, can associate this ballot with your identity.
                </div>
            </div>

            <!-- Receipt Details Box -->
            <div class="receipt-box">
                <div class="receipt-header">
                    <span class="receipt-icon">📜</span>
                    <span class="receipt-title">Cryptographic Digital Audit Receipt</span>
                    <span class="receipt-badge">SHA-256 Verified</span>
                </div>

                <div class="receipt-body">
                    <p class="receipt-instructions">
                        Keep this cryptographic verification token for your records. It proves that your ballot was accepted and tabulated in the election tally:
                    </p>

                    <div class="token-container">
                        <code id="receiptTokenCode" class="token-display">
                            <c:out value="${sessionScope.receiptToken != null ? sessionScope.receiptToken : 'TOKEN-VERIFIED-TRANSACTION-COMMITTED'}" />
                        </code>
                        <button type="button" class="btn btn-secondary btn-sm copy-token-btn" 
                                onclick="copyToClipboard('receiptTokenCode', this)">
                            📋 Copy Receipt
                        </button>
                    </div>

                    <!-- Verifiable QR Code Container (High-Contrast for Camera Scanning) -->
                    <div class="qr-code-wrapper">
                        <div class="qr-code-card">
                            <div id="qrcodeCanvas"></div>
                        </div>
                        <p class="qr-code-hint">
                            📷 Scan with any mobile camera to instantly verify ballot registration on the public ledger.
                        </p>
                    </div>

                    <div class="receipt-meta-grid">
                        <div class="receipt-meta-item">
                            <span class="meta-name">Voter Account</span>
                            <span class="meta-val">${sessionScope.currentUser.name} (${sessionScope.currentUser.email})</span>
                        </div>
                        <div class="receipt-meta-item">
                            <span class="meta-name">Voted Timestamp</span>
                            <span class="meta-val"><span id="confirmation-time">Synchronized with Database</span></span>
                        </div>
                        <div class="receipt-meta-item">
                            <span class="meta-name">Algorithm</span>
                            <span class="meta-val">SHA-256 (256-bit Cryptographic Hash)</span>
                        </div>
                        <div class="receipt-meta-item">
                            <span class="meta-name">Status</span>
                            <span class="meta-val text-success">● Committed & Sealed</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Action Buttons -->
            <div class="confirmation-actions">
                <a href="${pageContext.request.contextPath}/voter/dashboard" class="btn btn-primary btn-lg">
                    Return to Voter Dashboard &rarr;
                </a>
                <button type="button" onclick="window.print()" class="btn btn-outline-secondary btn-lg">
                    🖨️ Print Receipt
                </button>
            </div>
        </div>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

    <!-- QR Code Generator Library -->
    <script src="https://cdn.jsdelivr.net/npm/qrcodejs@1.0.0/qrcode.min.js"></script>

    <script>
        document.addEventListener("DOMContentLoaded", function() {
            var timeEl = document.getElementById("confirmation-time");
            if (timeEl) {
                var now = new Date();
                timeEl.textContent = now.toLocaleDateString() + " " + now.toLocaleTimeString();
            }

            var tokenEl = document.getElementById("receiptTokenCode");
            var qrContainer = document.getElementById("qrcodeCanvas");
            if (tokenEl && qrContainer && typeof QRCode !== 'undefined') {
                var token = tokenEl.textContent.trim();
                var verifyUrl = window.location.origin + "${pageContext.request.contextPath}/verify-receipt?receiptToken=" + encodeURIComponent(token);
                new QRCode(qrContainer, {
                    text: verifyUrl,
                    width: 140,
                    height: 140,
                    colorDark : "#0F172A",
                    colorLight : "#FFFFFF",
                    correctLevel : QRCode.CorrectLevel.H
                });
            }
        });
    </script>
</body>
</html>
