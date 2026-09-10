<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>NETRA | Create Case</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
<%@ include file="_nav.jsp" %>
<main class="page-shell">
    <section class="page-title-row"><div><span class="section-kicker">CASE INTAKE</span><h1>Create investigation</h1><p class="muted">Enter the case metadata and paste the raw report. NETRA will extract entities and merge them into the graph.</p></div></section>
    <c:if test="${not empty error}"><div class="flash flash-error">${error}</div></c:if>

    <div class="content-grid two-thirds">
        <article class="panel">
            <div class="panel-heading"><div><h2>Case details</h2></div><span class="muted">Required fields marked automatically by validation</span></div>
            <form action="${pageContext.request.contextPath}/admin/cases/new" method="post" class="form-grid">
                <label>Case number</label>
                <input type="text" name="caseNumber" placeholder="FIR-118/2026" required>

                <label>Investigation title</label>
                <input type="text" name="title" placeholder="Suspected financial and communication network" required>

                <label>Raw FIR / intelligence report</label>
                <textarea name="rawReportText" rows="15" placeholder="Paste the source text here. Example: Accused Rahul Sharma contacted Amit Patil near Mumbai Railway Station using 9876543210 and vehicle MH12AB1234."></textarea>

                <div class="form-actions">
                    <button type="submit" class="btn">Create &amp; Analyze Case</button>
                    <a href="${pageContext.request.contextPath}/admin/cases" class="btn btn-secondary">Cancel</a>
                </div>
            </form>
        </article>

        <aside class="panel">
            <div class="section-kicker">PIPELINE</div>
            <h2>What happens next?</h2>
            <div class="workflow">
                <div><b>01</b><span>Case saved to MySQL</span></div>
                <div><b>02</b><span>Java entity extraction runs on report text</span></div>
                <div><b>03</b><span>Person, phone and location nodes enter Neo4j</span></div>
                <div><b>04</b><span>Relationships become available for graph analysis</span></div>
            </div>
        </aside>
    </div>
</main>
</body>
</html>
