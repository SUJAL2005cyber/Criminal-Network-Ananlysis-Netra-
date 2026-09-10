<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>NETRA | Secure Sign In</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
<div class="login-shell">
    <div class="auth-card">
        <section class="auth-brand">
            <span class="brand-mark">N</span>
            <div class="eyebrow">LAW-ENFORCEMENT INTELLIGENCE PLATFORM</div>
            <h1>NETRA</h1>
            <p>AI-powered criminal network analysis for connecting cases, entities, evidence and relationships in one investigation workspace.</p>
            <div class="auth-points">
                <div class="auth-point"><b>01</b><span>Structured case intelligence with MySQL + Hibernate.</span></div>
                <div class="auth-point"><b>02</b><span>Relationship mapping and graph analysis with Neo4j.</span></div>
                <div class="auth-point"><b>03</b><span>Role-based access with Spring Security and BCrypt.</span></div>
            </div>
        </section>

        <section class="auth-form">
            <div class="section-kicker">SECURE ACCESS</div>
            <h2>Sign in to NETRA</h2>
            <p class="subtitle">Use your investigator or analyst account to continue.</p>

            <c:if test="${not empty error}"><div class="error-msg">${error}</div></c:if>
            <c:if test="${not empty success}"><div class="success-msg">${success}</div></c:if>

            <form action="${pageContext.request.contextPath}/admin/login" method="post" class="form-grid">
                <label>Username</label>
                <input type="text" name="username" required autofocus autocomplete="username">

                <label>Password</label>
                <input type="password" name="password" required autocomplete="current-password">

                <div class="form-actions">
                    <button type="submit" class="btn">Sign In →</button>
                </div>
            </form>

            <p class="note">Need an account? <a class="text-link" href="${pageContext.request.contextPath}/admin/register">Create investigator access</a></p>
        </section>
    </div>
</div>
</body>
</html>
