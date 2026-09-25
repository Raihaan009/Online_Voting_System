<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Voter Authentication - CampusVote | Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">

    <!-- Include Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container auth-container">
        <div class="auth-card">
            <!-- Header Icon & Title -->
            <div class="auth-header">
                <div class="brand-hierarchy-badge">
                    <span class="brand-title-mini">CampusVote</span>
                    <span class="brand-secondary-text">ONLINE VOTING SYSTEM</span>
                </div>
                <div class="auth-icon-badge">👤</div>
                <h2>Voter Sign In</h2>
                <p>Access your institutional ballot and active campus elections</p>
            </div>

            <!-- Notification Alerts -->
            <c:if test="${not empty param.error}">
                <div class="alert alert-danger" role="alert">
                    <span class="alert-icon">⚠️</span>
                    <div class="alert-content">
                        <c:choose>
                            <c:when test="${param.error == 'unauthorized'}">
                                Access denied. Please log in with your voter account to access that page.
                            </c:when>
                            <c:when test="${param.error == 'invalid_credentials'}">
                                Invalid email or password. Please verify your credentials and try again.
                            </c:when>
                            <c:when test="${param.error == 'account_suspended'}">
                                Your account is currently suspended. Please contact the Election Commission.
                            </c:when>
                            <c:when test="${param.error == 'empty_fields'}">
                                Please enter both your email address and password.
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
                    <div class="alert-content">
                        <c:choose>
                            <c:when test="${param.success == 'registered'}">
                                Registration completed successfully! You can now log in to participate.
                            </c:when>
                            <c:otherwise>
                                <c:out value="${param.success}" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty param.info}">
                <div class="alert alert-info" role="alert">
                    <span class="alert-icon">ℹ️</span>
                    <div class="alert-content">
                        <c:out value="${param.info}" />
                    </div>
                </div>
            </c:if>

            <!-- Voter Authentication Form -->
            <form action="${pageContext.request.contextPath}/login" method="POST" class="auth-form" id="loginForm">
                <div class="form-group">
                    <label for="email" class="form-label">Institutional Email</label>
                    <div class="input-with-icon">
                        <span class="input-icon">✉️</span>
                        <input type="email" id="email" name="email" class="form-control" 
                               placeholder="e.g. student@college.edu" required autocomplete="email" autofocus>
                    </div>
                </div>

                <div class="form-group">
                    <label for="password" class="form-label">Password</label>
                    <div class="input-with-icon">
                        <span class="input-icon">🔑</span>
                        <input type="password" id="password" name="password" class="form-control" 
                               placeholder="Enter your secure password" required autocomplete="current-password">
                    </div>
                </div>

                <button type="submit" class="btn btn-primary btn-block btn-lg" id="loginSubmitBtn">
                    Sign In to Vote &rarr;
                </button>
            </form>

            <!-- Card Footer Links -->
            <div class="auth-footer">
                <p>New eligible voter? <a href="${pageContext.request.contextPath}/register.jsp" class="auth-link">Register your account</a></p>
                <div class="auth-divider"><span>OR</span></div>
                <p><a href="${pageContext.request.contextPath}/admin/login.jsp" class="admin-switch-link">Election Administrator Login &rarr;</a></p>
            </div>
        </div>
    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
