<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>NETRA | Register</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
<div class="login-shell">
    <div class="auth-card">
        <section class="auth-brand">
            <span class="brand-mark">N</span>
            <div class="eyebrow">INVESTIGATOR ONBOARDING</div>
            <h1>Secure access.</h1>
            <p>Create a NETRA user profile. Passwords are protected with BCrypt before they reach the database.</p>
            <div class="auth-points">
                <div class="auth-point"><b>ROLE</b><span>Investigator, analyst or administrator permissions.</span></div>
                <div class="auth-point"><b>DATA</b><span>Case records remain in the structured MySQL store.</span></div>
                <div class="auth-point"><b>GRAPH</b><span>Connected entities are analyzed in Neo4j.</span></div>
            </div>
        </section>

        <section class="auth-form">
            <div class="section-kicker">CREATE ACCOUNT</div>
            <h2>Register user</h2>
            <p class="subtitle">For the SIH demonstration environment.</p>

            <c:if test="${not empty error}"><div class="error-msg">${error}</div></c:if>

            <form action="${pageContext.request.contextPath}/admin/register" method="post" class="form-grid">
                <label>Full name</label>
                <input type="text" name="fullName" required>

                <label>Badge number</label>
                <input type="text" name="badgeNumber" required>

                <label>Role</label>
                <select name="role">
                    <option value="INVESTIGATOR">Investigator</option>
                    <option value="ANALYST">Analyst</option>
                    <option value="ADMIN">Administrator</option>
                </select>

                <label>Username</label>
                <input type="text" name="username" required autocomplete="username">

                <label>Password</label>
                <input type="password" name="password" minlength="6" required autocomplete="new-password">

                <div class="form-actions">
                    <button type="submit" class="btn">Create account</button>
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/admin/login">Back to sign in</a>
                </div>
            </form>
        </section>
    </div>
</div>
</body>
</html>
