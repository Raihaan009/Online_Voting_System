<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%-- Defensive redirect if accessed directly without servlet --%>
<c:if test="${empty voter && empty sessionScope.currentUser}">
    <c:redirect url="/login.jsp" />
</c:if>

<c:set var="voter" value="${not empty voter ? voter : sessionScope.currentUser}" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mandatory Student Profile - CampusVote | Online Voting System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="dashboard-page">

    <!-- Include Navigation Bar -->
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <main class="container profile-page-container" style="max-width: 760px; margin: 2rem auto; padding: 0 1rem;">

        <!-- Breadcrumb / Back Link -->
        <div class="breadcrumb-bar" style="margin-bottom: 1.25rem;">
            <a href="${pageContext.request.contextPath}/voter/dashboard" class="nav-back-link" style="color: var(--accent-primary); text-decoration: none; font-weight: 600; font-size: 0.9rem;">
                &larr; Return to Voter Dashboard
            </a>
        </div>

        <!-- Instructional Gatekeeper Warning Banner -->
        <c:if test="${not empty param.warning}">
            <div class="alert alert-warning" role="alert" style="margin-bottom: 1.5rem;">
                <span class="alert-icon">⚠️</span>
                <div class="alert-content">
                    <strong>Mandatory Profile Verification:</strong>
                    <c:out value="${param.warning}" />
                </div>
            </div>
        </c:if>

        <!-- Error Feedback -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger" role="alert" style="margin-bottom: 1.5rem;">
                <span class="alert-icon">⚠️</span>
                <div class="alert-content"><c:out value="${error}" /></div>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger" role="alert" style="margin-bottom: 1.5rem;">
                <span class="alert-icon">⚠️</span>
                <div class="alert-content"><c:out value="${param.error}" /></div>
            </div>
        </c:if>

        <!-- Success Feedback -->
        <c:if test="${not empty param.success}">
            <div class="alert alert-success" role="alert" style="margin-bottom: 1.5rem;">
                <span class="alert-icon">✓</span>
                <div class="alert-content"><c:out value="${param.success}" /></div>
            </div>
        </c:if>

        <!-- Profile Form Card -->
        <div class="profile-card" style="background: var(--surface-card); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 2.25rem; box-shadow: var(--shadow-sm);">

            <!-- Profile Header -->
            <div class="profile-card-header" style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 1.5rem; margin-bottom: 1.75rem;">
                <div>
                    <div class="banner-pill" style="margin-bottom: 0.5rem;">Student Electorate Verification</div>
                    <h1 style="font-size: 1.6rem; font-weight: 800; color: var(--text-primary); margin: 0 0 0.35rem 0;">
                        Official Voter Profile
                    </h1>
                    <p style="color: var(--text-muted); font-size: 0.9rem; margin: 0;">
                        Institutional electoral compliance requires accurate student demographics before casting democratic ballots.
                    </p>
                </div>

                <!-- Status Badge Indicator -->
                <div>
                    <c:choose>
                        <c:when test="${voter.profileComplete}">
                            <span class="badge badge-success" style="font-size: 0.825rem; padding: 0.4rem 0.85rem; display: inline-flex; align-items: center; gap: 0.35rem;">
                                ✓ Profile Complete
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge badge-danger" style="font-size: 0.825rem; padding: 0.4rem 0.85rem; display: inline-flex; align-items: center; gap: 0.35rem;">
                                ⚠️ Profile Incomplete
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Profile Completion Notice if incomplete -->
            <c:if test="${!voter.profileComplete}">
                <div style="background: var(--accent-light); border: 1px solid var(--border-color); border-left: 4px solid var(--accent-primary); border-radius: var(--radius-sm); padding: 0.85rem 1.15rem; margin-bottom: 1.75rem;">
                    <div style="font-size: 0.875rem; color: var(--text-primary); line-height: 1.45;">
                        <strong>🔒 Voting Booth Locked:</strong> You will not be permitted to enter any election booths until this official student profile is completed and verified.
                    </div>
                </div>
            </c:if>

            <!-- Form -->
            <form action="${pageContext.request.contextPath}/voter/profile" method="POST" class="auth-form profile-form">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">

                <!-- Full Name -->
                <div class="form-group" style="margin-bottom: 1.35rem;">
                    <label class="form-label" for="voterName" style="display: block; font-weight: 600; color: var(--text-primary); margin-bottom: 0.4rem;">
                        Full Legal / Campus Name <span style="color: var(--danger);">*</span>
                    </label>
                    <input type="text" id="voterName" name="name" class="form-control"
                           value="<c:out value="${not empty param.name ? param.name : voter.name}" />"
                           placeholder="e.g. Jordan Smith"
                           required
                           style="width: 100%; padding: 0.75rem 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); background: var(--input-bg); color: var(--text-primary); font-size: 0.95rem;">
                    <small style="display: block; color: var(--text-muted); font-size: 0.8rem; margin-top: 0.3rem;">
                        This name is cross-referenced with your official institutional admission record.
                    </small>
                </div>

                <!-- Institutional Email (Read-Only) -->
                <div class="form-group" style="margin-bottom: 1.35rem;">
                    <label class="form-label" for="voterEmail" style="display: block; font-weight: 600; color: var(--text-primary); margin-bottom: 0.4rem;">
                        Institutional Email Address (Permanent Identity)
                    </label>
                    <input type="email" id="voterEmail" value="<c:out value="${voter.email}" />" class="form-control" disabled readonly
                           style="width: 100%; padding: 0.75rem 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); background: var(--bg-body); color: var(--text-muted); font-size: 0.95rem; cursor: not-allowed;">
                    <small style="display: block; color: var(--text-muted); font-size: 0.8rem; margin-top: 0.3rem;">
                        🔒 Cryptographically linked to your voting credentials and cannot be edited.
                    </small>
                </div>

                <!-- Age & Academic Year (Grid Layout) -->
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.25rem; margin-bottom: 1.35rem;">
                    
                    <!-- Age -->
                    <div class="form-group">
                        <label class="form-label" for="voterAge" style="display: block; font-weight: 600; color: var(--text-primary); margin-bottom: 0.4rem;">
                            Age (Years) <span style="color: var(--danger);">*</span>
                        </label>
                        <input type="number" id="voterAge" name="age" min="17" max="99" class="form-control"
                               value="<c:out value="${not empty param.age ? param.age : (voter.age > 0 ? voter.age : '')}" />"
                               placeholder="e.g. 20"
                               required
                               style="width: 100%; padding: 0.75rem 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); background: var(--input-bg); color: var(--text-primary); font-size: 0.95rem;">
                        <small style="display: block; color: var(--text-muted); font-size: 0.8rem; margin-top: 0.3rem;">
                            Eligible voting range: 17 – 99 years.
                        </small>
                    </div>

                    <!-- Academic Year -->
                    <div class="form-group">
                        <label class="form-label" for="voterAcademicYear" style="display: block; font-weight: 600; color: var(--text-primary); margin-bottom: 0.4rem;">
                            Academic Year Standing <span style="color: var(--danger);">*</span>
                        </label>
                        <select id="voterAcademicYear" name="academicYear" class="form-control" required
                                style="width: 100%; padding: 0.75rem 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); background: var(--input-bg); color: var(--text-primary); font-size: 0.95rem;">
                            <option value="" disabled ${empty voter.academicYear && empty param.academicYear ? 'selected' : ''}>
                                -- Select Academic Year --
                            </option>
                            <c:set var="curYear" value="${not empty param.academicYear ? param.academicYear : voter.academicYear}" />
                            <option value="First Year (FE)" ${curYear == 'First Year (FE)' || curYear == 'FE' ? 'selected' : ''}>First Year (FE)</option>
                            <option value="Second Year (SE)" ${curYear == 'Second Year (SE)' || curYear == 'SE' ? 'selected' : ''}>Second Year (SE)</option>
                            <option value="Third Year (TE)" ${curYear == 'Third Year (TE)' || curYear == 'TE' ? 'selected' : ''}>Third Year (TE)</option>
                            <option value="Final Year (BE)" ${curYear == 'Final Year (BE)' || curYear == 'BE' || curYear == 'Final Year' ? 'selected' : ''}>Final Year (BE)</option>
                        </select>
                        <small style="display: block; color: var(--text-muted); font-size: 0.8rem; margin-top: 0.3rem;">
                            Your current active academic standing.
                        </small>
                    </div>
                </div>

                <!-- Academic Department / Branch -->
                <div class="form-group" style="margin-bottom: 2rem;">
                    <label class="form-label" for="voterBranch" style="display: block; font-weight: 600; color: var(--text-primary); margin-bottom: 0.4rem;">
                        Department / Branch of Study <span style="color: var(--danger);">*</span>
                    </label>
                    <c:set var="curBranch" value="${not empty param.branch ? param.branch : (not empty voter.branch ? voter.branch : voter.department)}" />
                    <select id="voterBranch" name="branch" class="form-control" required
                            style="width: 100%; padding: 0.75rem 1rem; border-radius: var(--radius-md); border: 1px solid var(--border-color); background: var(--input-bg); color: var(--text-primary); font-size: 0.95rem;">
                        <option value="" disabled ${empty curBranch ? 'selected' : ''}>
                            -- Select Department / Branch --
                        </option>
                        <option value="Computer Engineering" ${curBranch == 'Computer Engineering' || curBranch == 'Computer Science' ? 'selected' : ''}>Computer Engineering</option>
                        <option value="Information Technology" ${curBranch == 'Information Technology' ? 'selected' : ''}>Information Technology</option>
                        <option value="AI & Data Science" ${curBranch == 'AI & Data Science' ? 'selected' : ''}>AI & Data Science</option>
                        <option value="Electronics & Telecommunication (EXTC)" ${curBranch == 'Electronics & Telecommunication (EXTC)' || curBranch == 'Electronics & Telecom' ? 'selected' : ''}>Electronics & Telecommunication (EXTC)</option>
                        <option value="Mechanical Engineering" ${curBranch == 'Mechanical Engineering' || curBranch == 'Mechanical' ? 'selected' : ''}>Mechanical Engineering</option>
                        <option value="Civil Engineering" ${curBranch == 'Civil Engineering' || curBranch == 'Civil' ? 'selected' : ''}>Civil Engineering</option>
                        <option value="Electrical Engineering" ${curBranch == 'Electrical Engineering' || curBranch == 'Electrical' ? 'selected' : ''}>Electrical Engineering</option>
                    </select>
                    <small style="display: block; color: var(--text-muted); font-size: 0.8rem; margin-top: 0.3rem;">
                        Used to configure demographic turnout telemetry and constituency eligibility.
                    </small>
                </div>

                <!-- Action Button Bar -->
                <div class="form-actions" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; border-top: 1px solid var(--border-color); padding-top: 1.5rem;">
                    <a href="${pageContext.request.contextPath}/voter/dashboard" class="btn btn-outline-secondary" style="padding: 0.75rem 1.35rem;">
                        Cancel
                    </a>

                    <button type="submit" class="btn btn-primary" id="saveProfileBtn" style="padding: 0.75rem 1.75rem; font-weight: 700; display: inline-flex; align-items: center; gap: 0.5rem;">
                        <span>Save & Complete Profile &rarr;</span>
                    </button>
                </div>

            </form>
        </div>

    </main>

    <!-- Include Footer -->
    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

</body>
</html>
