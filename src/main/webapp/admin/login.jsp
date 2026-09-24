<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Administrator Login - Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page admin-auth-bg">

    <!-- Include Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container auth-container">
        <div class="auth-card admin-auth-card">
            <!-- Header Icon & Title -->
            <div class="auth-header">
                <div class="auth-icon-badge admin-badge-lg">🛡️</div>
                <h2>Admin Console Login</h2>
                <p>Restricted access for certified Election Commission officials</p>
            </div>

            <!-- Alerts -->
            <c:if test="${not empty param.info}">
                <div class="alert alert-info" role="alert">
                    <span class="alert-icon">ℹ️</span>
                    <div class="alert-content">
                        <c:out value="${param.info}" />
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty param.error}">
                <div class="alert alert-danger" role="alert">
                    <span class="alert-icon">⚠️</span>
                    <div class="alert-content">
                        <c:choose>
                            <c:when test="${param.error == 'unauthorized'}">
                                Administrator authentication required to access the requested control panel.
                            </c:when>
                            <c:otherwise>
                                <c:out value="${param.error}" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <!-- Administrator Login Form -->
            <form action="${pageContext.request.contextPath}/admin/login" method="POST" class="auth-form" id="adminLoginForm">
                <div class="form-group">
                    <label for="adminEmail" class="form-label">Administrator Email</label>
                    <div class="input-with-icon">
                        <span class="input-icon">✉️</span>
                        <input type="email" id="adminEmail" name="email" class="form-control" 
                               placeholder="e.g. admin@college.edu" required autocomplete="email" autofocus>
                    </div>
                </div>

                <div class="form-group">
                    <label for="adminPassword" class="form-label">Master Password</label>
                    <div class="input-with-icon">
                        <span class="input-icon">🔑</span>
                        <input type="password" id="adminPassword" name="password" class="form-control" 
                               placeholder="Enter your administrative password" required autocomplete="current-password">
                    </div>
                </div>

                <div class="admin-security-notice">
                    <span>🔒</span>
                    <small>All administrator access and tabulation activities are strictly logged and audited.</small>
                </div>

                <button type="submit" class="btn btn-purple btn-block btn-lg" id="adminLoginSubmitBtn">
                    Authenticate Administrator &rarr;
                </button>
            </form>

            <!-- Card Footer Links -->
            <div class="auth-footer">
                <p><a href="${pageContext.request.contextPath}/login.jsp" class="auth-link">&larr; Return to Student Voter Login</a></p>
            </div>
        </div>
    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
