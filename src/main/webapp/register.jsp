<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Voter Registration - Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-page">

    <!-- Include Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container auth-container">
        <div class="auth-card register-card">
            <!-- Header Icon & Title -->
            <div class="auth-header">
                <div class="auth-icon-badge register-badge">📝</div>
                <h2>Voter Registration</h2>
                <p>Register as an eligible voter for institutional student council elections</p>
            </div>

            <!-- Error Alerts -->
            <c:if test="${not empty param.error}">
                <div class="alert alert-danger" role="alert">
                    <span class="alert-icon">⚠️</span>
                    <div class="alert-content">
                        <c:choose>
                            <c:when test="${param.error == 'email_exists'}">
                                This email address is already registered. Please sign in or use another email.
                            </c:when>
                            <c:when test="${param.error == 'registration_failed'}">
                                Registration could not be completed. Please check your details and try again.
                            </c:when>
                            <c:otherwise>
                                <c:out value="${param.error}" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

            <!-- Registration Form -->
            <form action="${pageContext.request.contextPath}/register" method="POST" class="auth-form" id="registerForm">
                <!-- Full Name -->
                <div class="form-group">
                    <label for="name" class="form-label">Full Name</label>
                    <div class="input-with-icon">
                        <span class="input-icon">👤</span>
                        <input type="text" id="name" name="name" class="form-control" 
                               placeholder="e.g. Alex Morgan" required autocomplete="name" autofocus>
                    </div>
                </div>

                <!-- Institutional Email -->
                <div class="form-group">
                    <label for="regEmail" class="form-label">Institutional Email</label>
                    <div class="input-with-icon">
                        <span class="input-icon">✉️</span>
                        <input type="email" id="regEmail" name="email" class="form-control" 
                               placeholder="e.g. alex.morgan@college.edu" required autocomplete="email">
                    </div>
                    <small class="form-hint">Must be your authorized campus/institutional email address.</small>
                </div>

                <!-- Password -->
                <div class="form-group">
                    <label for="regPassword" class="form-label">Password</label>
                    <div class="input-with-icon">
                        <span class="input-icon">🔑</span>
                        <input type="password" id="regPassword" name="password" class="form-control" 
                               placeholder="Minimum 6 characters" required minlength="6" autocomplete="new-password">
                    </div>
                </div>

                <!-- Confirm Password with Live Validation Indicator -->
                <div class="form-group">
                    <label for="confirmPassword" class="form-label">Confirm Password</label>
                    <div class="input-with-icon">
                        <span class="input-icon">🔒</span>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                               placeholder="Re-enter your password" required minlength="6" autocomplete="new-password">
                    </div>
                    <div id="passwordMatchStatus" class="match-feedback"></div>
                </div>

                <div class="form-note">
                    <span>🛡️</span>
                    <small>Your password is salted and encrypted using 12-round BCrypt before storage.</small>
                </div>

                <button type="submit" class="btn btn-primary btn-block btn-lg" id="registerSubmitBtn">
                    Create Voter Account &rarr;
                </button>
            </form>

            <!-- Card Footer Links -->
            <div class="auth-footer">
                <p>Already have an account? <a href="${pageContext.request.contextPath}/login.jsp" class="auth-link">Sign in here</a></p>
            </div>
        </div>
    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
