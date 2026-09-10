<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>NETRA | Cases</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
<%@ include file="_nav.jsp" %>
<main class="page-shell">
    <section class="page-title-row">
        <div><span class="section-kicker">CASE MANAGEMENT</span><h1>Investigation cases</h1><p class="muted">Create, review and analyze criminal reports.</p></div>
        <a href="${pageContext.request.contextPath}/admin/cases/new" class="btn">+ New Case</a>
    </section>

    <c:if test="${not empty success}"><div class="flash flash-success">${success}</div></c:if>

    <div class="panel">
        <div class="panel-heading"><div><h2>All cases</h2></div><span class="muted">${cases.size()} records</span></div>
        <div class="table-wrap">
            <table>
                <thead><tr><th>Case no.</th><th>Title</th><th>Status</th><th>Created</th><th>Created by</th></tr></thead>
                <tbody>
                <c:forEach var="c" items="${cases}">
                    <tr class="clickable" onclick="window.location='${pageContext.request.contextPath}/admin/cases/${c.id}'">
                        <td><strong>${c.caseNumber}</strong></td>
                        <td>${c.title}</td>
                        <td><span class="status-pill status-${c.status}">${c.status}</span></td>
                        <td class="muted">${c.createdAt}</td>
                        <td class="muted">${c.createdBy}</td>
                    </tr>
                </c:forEach>
                <c:if test="${empty cases}"><tr><td colspan="5" class="empty-row">No cases yet. Create the first investigation from the button above.</td></tr></c:if>
                </tbody>
            </table>
        </div>
    </div>
</main>
</body>
</html>
