<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<script>
    /* Immediate theme application to prevent FOUC / theme flash */
    (function () {
        try {
            var savedTheme = localStorage.getItem('campusvote_theme') || localStorage.getItem('theme') || 'light';
            document.documentElement.setAttribute('data-theme', savedTheme);
        } catch (e) {}
    })();
</script>

<!-- Global Navigation Bar -->
<header class="navbar">
    <div class="nav-container">
        <!-- Brand Logo & Title (Brand Hierarchy Refinement) -->
        <a href="${pageContext.request.contextPath}/" class="logo-brand">
            <div class="logo-badge">V</div>
            <div class="logo-text">
                <span class="logo-title">CampusVote</span>
                <span class="logo-sub brand-secondary-text">ONLINE VOTING SYSTEM</span>
            </div>
        </a>

        <!-- Navigation Links & User Authentication Status -->
        <nav class="nav-links">
            <a href="${pageContext.request.contextPath}/" class="nav-link">Home</a>
            <a href="${pageContext.request.contextPath}/verify-receipt" class="nav-link">Verify Ballot Receipt</a>

            <c:choose>
                <%-- Voter Authenticated Session --%>
                <c:when test="${not empty sessionScope.currentUser}">
                    <a href="${pageContext.request.contextPath}/voter/dashboard" class="nav-link">Voter Dashboard</a>
                    <div class="user-pill voter-pill">
                        <span class="user-avatar">👤</span>
                        <span class="user-name">${sessionScope.currentUser.name}</span>
                        <span class="role-badge voter-badge">Voter</span>
                    </div>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-danger btn-sm">Logout</a>
                </c:when>

                <%-- Administrator Authenticated Session --%>
                <c:when test="${not empty sessionScope.currentAdmin}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link">Admin Console</a>
                    <div class="user-pill admin-pill">
                        <span class="user-avatar">🛡️</span>
                        <span class="user-name">${sessionScope.currentAdmin.name}</span>
                        <span class="role-badge admin-badge">Admin</span>
                    </div>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-danger btn-sm">Logout</a>
                </c:when>

                <%-- Unauthenticated Guest User --%>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-outline-primary btn-sm">Voter Login</a>
                    <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-primary btn-sm">Register</a>
                    <a href="${pageContext.request.contextPath}/admin/login.jsp" class="nav-link admin-nav-link" title="Administrator Portal">Admin</a>
                </c:otherwise>
            </c:choose>

            <!-- Global Light / Dark Mode Toggle Button -->
            <button id="themeToggleBtn" type="button" class="theme-toggle-btn" aria-label="Toggle Theme" title="Toggle theme">
                <span id="themeIcon">🌙</span>
            </button>
            <script>
                (function() {
                    try {
                        var current = localStorage.getItem('campusvote_theme') || localStorage.getItem('theme') || 'light';
                        var icon = document.getElementById('themeIcon');
                        if (icon) {
                            icon.textContent = current === 'dark' ? '☀️' : '🌙';
                        }
                    } catch(e) {}
                })();
            </script>
        </nav>
    </div>
</header>

