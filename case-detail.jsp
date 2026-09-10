<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>NETRA | ${caseItem.caseNumber}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
<%@ include file="_nav.jsp" %>
<main class="page-shell">
    <div class="page-title-row">
        <div><span class="section-kicker">CASE FILE</span><h1>${caseItem.caseNumber}</h1><p class="muted">${caseItem.title}</p></div>
        <span class="status-pill status-${caseItem.status}">${caseItem.status}</span>
    </div>

    <c:if test="${not empty success}"><div class="flash flash-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="flash flash-error">${error}</div></c:if>

    <section class="content-grid two-thirds">
        <article class="panel">
            <div class="panel-heading"><div><span class="section-kicker">CASE RECORD</span><h2>Edit investigation</h2></div><span class="muted">Created ${caseItem.createdAt}</span></div>
            <form action="${pageContext.request.contextPath}/admin/cases/${caseItem.id}/update" method="post" class="form-grid">
                <label>Title</label>
                <input type="text" name="title" value="${caseItem.title}" required>

                <label>Status</label>
                <select name="status">
                    <option value="OPEN" ${caseItem.status == 'OPEN' ? 'selected' : ''}>OPEN</option>
                    <option value="UNDER_INVESTIGATION" ${caseItem.status == 'UNDER_INVESTIGATION' ? 'selected' : ''}>UNDER INVESTIGATION</option>
                    <option value="CLOSED" ${caseItem.status == 'CLOSED' ? 'selected' : ''}>CLOSED</option>
                </select>

                <label>Raw report text</label>
                <textarea name="rawReportText" rows="15">${caseItem.rawReportText}</textarea>

                <div class="form-actions">
                    <button type="submit" class="btn">Save changes</button>
                    <button type="submit" form="extract-form" class="btn btn-secondary">Re-run extraction</button>
                </div>
            </form>

            <form id="extract-form" action="${pageContext.request.contextPath}/admin/cases/${caseItem.id}/extract" method="post"></form>
        </article>

        <aside class="panel">
            <div class="section-kicker">CASE INTELLIGENCE</div>
            <h2>Analysis ready</h2>
            <p class="muted">The report text can be re-processed at any time to update the network with extracted entities.</p>
            <div class="detail-meta"><span>CASE NUMBER</span><strong>${caseItem.caseNumber}</strong></div>
            <div class="detail-meta"><span>CREATED BY</span><strong>${caseItem.createdBy}</strong></div>
            <div class="detail-meta"><span>STATUS</span><strong>${caseItem.status}</strong></div>
            <a class="btn" href="${pageContext.request.contextPath}/admin/network">Open network analysis</a>

            <div class="danger-zone">
                <div><strong>Delete case</strong><span>This removes the relational case record.</span></div>
                <form action="${pageContext.request.contextPath}/admin/cases/${caseItem.id}/delete" method="post" onsubmit="return confirm('Delete this case? This cannot be undone.');">
                    <button type="submit" class="btn btn-danger">Delete</button>
                </form>
            </div>
        </aside>
    </section>
</main>
</body>
</html>
