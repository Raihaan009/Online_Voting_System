<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 - Page Not Found | CampusVote - Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Include Reusable Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container page-container" style="max-width: 680px; padding: 4rem 1rem; text-align: center;">

        <div class="empty-state-card" style="padding: 3.5rem 2rem; border-radius: var(--radius-xl); box-shadow: var(--shadow-lg);">
            <div style="font-size: 4rem; line-height: 1; margin-bottom: 1rem;">🔍</div>
            <div class="banner-pill" style="background: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.3); display: inline-block; padding: 0.35rem 0.85rem; border-radius: 9999px; font-size: 0.85rem; font-weight: 700; margin-bottom: 1rem;">
                HTTP 404 &bull; Resource Not Found
            </div>
            
            <h1 style="font-size: 2rem; font-weight: 900; color: var(--text-primary); margin-bottom: 1rem;">
                Page Not Found
            </h1>
            
            <p style="color: var(--text-secondary); font-size: 1rem; line-height: 1.6; margin-bottom: 2rem;">
                The electoral resource, voting portal endpoint, or administrative view you requested does not exist or may have been relocated.
            </p>

            <div style="display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-lg">
                    &larr; Return to Home
                </a>
                <a href="${pageContext.request.contextPath}/voter/dashboard" class="btn btn-outline-secondary btn-lg">
                    Voter Dashboard
                </a>
                <a href="${pageContext.request.contextPath}/verify-receipt" class="btn btn-purple btn-lg">
                    Verify Ballot
                </a>
            </div>
        </div>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
