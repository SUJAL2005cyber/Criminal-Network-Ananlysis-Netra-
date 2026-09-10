<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="path" value="${pageContext.request.servletPath}" />

<header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/admin/dashboard">
        <span class="brand-mark">N</span>
        <span>
            <strong>NETRA</strong>
            <small>Network Intelligence</small>
        </span>
    </a>

    <nav class="main-nav">
        <a class="${path == '/admin/dashboard' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
        <a class="${path.startsWith('/admin/cases') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/cases">Cases</a>
        <a class="${path.startsWith('/admin/network') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/network">Network Analysis</a>
    </nav>

    <div class="user-menu">
        <div class="avatar">${not empty username ? username.substring(0,1).toUpperCase() : 'U'}</div>
        <div class="user-meta">
            <strong>${username}</strong>
            <span>${role}</span>
        </div>
        <a class="logout" href="${pageContext.request.contextPath}/admin/logout">Logout</a>
    </div>
</header>
