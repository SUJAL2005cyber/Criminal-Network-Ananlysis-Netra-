<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>NETRA | Command Dashboard</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
<%@ include file="_nav.jsp" %>

<main class="page-shell">
    <section class="hero">
        <div>
            <div class="eyebrow">INVESTIGATION COMMAND CENTER</div>
            <h1>Welcome back, ${username}.</h1>
            <p>Analyze case intelligence, uncover hidden relationships, and prioritize key network actors.</p>
        </div>
        <div class="hero-actions">
            <a class="btn" href="${pageContext.request.contextPath}/admin/cases/new">+ Create Case</a>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/admin/network">Open Network</a>
        </div>
    </section>

    <c:if test="${not empty success}">
        <div class="flash flash-success">${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="flash flash-error">${error}</div>
    </c:if>

    <section class="kpi-grid">
        <article class="kpi-card accent-blue">
            <span class="kpi-icon">◉</span>
            <div><span class="kpi-label">Persons tracked</span><strong>${totalPersons}</strong></div>
            <small>Graph entities</small>
        </article>
        <article class="kpi-card accent-red">
            <span class="kpi-icon">⌁</span>
            <div><span class="kpi-label">Relationships</span><strong>${totalRelationships}</strong></div>
            <small>Confirmed links</small>
        </article>
        <article class="kpi-card accent-amber">
            <span class="kpi-icon">!</span>
            <div><span class="kpi-label">Open cases</span><strong>${openCases}</strong></div>
            <small>Require attention</small>
        </article>
        <article class="kpi-card accent-green">
            <span class="kpi-icon">✓</span>
            <div><span class="kpi-label">Investigating</span><strong>${investigations}</strong></div>
            <small>Active investigations</small>
        </article>
    </section>

    <section class="content-grid two-thirds">
        <article class="panel">
            <div class="panel-heading">
                <div><span class="section-kicker">CASE OPERATIONS</span><h2>Recent investigations</h2></div>
                <a class="text-link" href="${pageContext.request.contextPath}/admin/cases">View all →</a>
            </div>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>Case</th><th>Title</th><th>Status</th><th>Created</th></tr></thead>
                    <tbody>
                    <c:forEach var="c" items="${cases}" end="5">
                        <tr class="clickable" onclick="window.location='${pageContext.request.contextPath}/admin/cases/${c.id}'">
                            <td><strong>${c.caseNumber}</strong></td>
                            <td>${c.title}</td>
                            <td><span class="status-pill status-${c.status}">${c.status}</span></td>
                            <td class="muted">${c.createdAt}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty cases}">
                        <tr><td colspan="4" class="empty-row">No cases yet. Create your first investigation to populate the dashboard.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </article>

        <aside class="panel priority-panel">
            <div class="panel-heading">
                <div><span class="section-kicker">NETWORK SIGNAL</span><h2>Top influencers</h2></div>
                <a class="text-link" href="${pageContext.request.contextPath}/admin/network">Analyze →</a>
            </div>
            <p class="muted intro">People ranked by direct relationship count in the Java Collections graph engine.</p>
            <div class="ranking-list">
                <c:forEach var="entry" items="${topInfluencers}" varStatus="loop">
                    <div class="rank-row">
                        <span class="rank">${loop.index + 1}</span>
                        <div class="rank-person"><strong>${entry.key}</strong><span>Network actor</span></div>
                        <span class="rank-score">${entry.value}</span>
                    </div>
                </c:forEach>
                <c:if test="${empty topInfluencers}">
                    <div class="empty-state">No graph relationships recorded yet.</div>
                </c:if>
            </div>
        </aside>
    </section>

    <section class="content-grid three">
        <article class="info-card"><span class="mini-icon">01</span><div><h3>Structured case store</h3><p>MySQL + JPA/Hibernate keeps investigation metadata consistent and searchable.</p></div></article>
        <article class="info-card"><span class="mini-icon">02</span><div><h3>Relationship graph</h3><p>Neo4j stores people, phones, locations and evidence-aware associations.</p></div></article>
        <article class="info-card"><span class="mini-icon">03</span><div><h3>Explainable analysis</h3><p>Java graph algorithms surface paths, connections and high-degree network actors.</p></div></article>
    </section>
</main>
</body>
</html>
