<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>NETRA | Network Analysis</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/admin.css">
</head>
<body>
<%@ include file="_nav.jsp" %>
<main class="page-shell">
    <section class="page-title-row">
        <div>
            <span class="section-kicker">GRAPH INTELLIGENCE</span>
            <h1>Network analysis</h1>
            <p class="muted">Extract entities, confirm relationships and trace connections across the criminal network.</p>
        </div>
    </section>

    <c:if test="${not empty success}"><div class="flash flash-success">${success}</div></c:if>
    <c:if test="${not empty error}"><div class="flash flash-error">${error}</div></c:if>

    <section class="content-grid two-thirds">
        <article class="panel">
            <div class="panel-heading">
                <div><span class="section-kicker">ENTITY INGESTION</span><h2>Analyze raw report</h2></div>
            </div>
            <p class="muted intro">Java-based extraction identifies people, Indian phone numbers, locations and vehicle numbers, then merges person entities into Neo4j and prepares the analysis view.</p>
            <form action="${pageContext.request.contextPath}/admin/network/ingest" method="post" class="form-grid">
                <label>Case number</label>
                <input type="text" name="caseNumber" placeholder="NETRA-FIR-2026-001" required>
                <label>Raw intelligence text</label>
                <textarea name="rawText" rows="7" placeholder="Accused Rahul Sharma met Amit Patil near Mumbai Railway Station using 9876543210 and vehicle MH12AB1234." required></textarea>
                <div class="form-actions"><button type="submit" class="btn">Extract &amp; analyze</button></div>
            </form>
        </article>

        <article class="panel">
            <div class="panel-heading"><div><span class="section-kicker">RELATIONSHIP EVIDENCE</span><h2>Confirm a link</h2></div></div>
            <form action="${pageContext.request.contextPath}/admin/network/link" method="post" class="form-grid">
                <label>Person A</label><input type="text" name="personA" placeholder="Rahul Sharma" required>
                <label>Person B</label><input type="text" name="personB" placeholder="Amit Patil" required>
                <label>Relation type</label><input type="text" name="relationType" placeholder="CALL_RECORD / ACCOMPLICE / FINANCIAL">
                <label>Evidence source</label><input type="text" name="evidenceSource" placeholder="CDR-2026-0031">
                <label>Strength (0.0 - 1.0)</label><input type="number" name="strength" min="0" max="1" step="0.1" value="0.8">
                <div class="form-actions"><button type="submit" class="btn">Create relationship</button></div>
            </form>
        </article>
    </section>

    <c:if test="${not empty analysisResult}">
        <section class="analysis-hero">
            <div>
                <span class="section-kicker">AUTOMATED CASE ANALYSIS</span>
                <h2>Case ${analysisResult.caseNumber} — analysis ready</h2>
                <p class="muted">One-click result: entities, graph, statistics, path, priority actor, evidence and an investigator-facing summary.</p>
            </div>
            <div class="analysis-badge"><span>PROVISIONAL LEADS</span><strong>VERIFY</strong></div>
        </section>

        <section class="stat-grid">
            <div class="stat-card"><span>Persons</span><strong>${analysisResult.personCount}</strong><small>Extracted</small></div>
            <div class="stat-card"><span>Phones</span><strong>${analysisResult.phoneCount}</strong><small>Extracted</small></div>
            <div class="stat-card"><span>Locations</span><strong>${analysisResult.locationCount}</strong><small>Extracted</small></div>
            <div class="stat-card"><span>Vehicles</span><strong>${analysisResult.vehicleCount}</strong><small>Extracted</small></div>
            <div class="stat-card"><span>Graph persons</span><strong>${analysisResult.totalPersons}</strong><small>Current graph</small></div>
            <div class="stat-card"><span>Relationships</span><strong>${analysisResult.totalRelationships}</strong><small>Current graph</small></div>
        </section>

        <section class="content-grid two-thirds analysis-grid">
            <article class="panel">
                <div class="panel-heading"><div><span class="section-kicker">NETWORK MAP</span><h2>Relationship graph</h2></div><span class="muted">Java Collections snapshot</span></div>
                <div class="network-map">
                    <svg viewBox="0 0 840 420" role="img" aria-label="Network graph">
                        <defs>
                            <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
                                <feGaussianBlur stdDeviation="4" result="blur"/>
                                <feMerge><feMergeNode in="blur"/><feMergeNode in="SourceGraphic"/></feMerge>
                            </filter>
                        </defs>
                        <c:forEach var="edge" items="${analysisResult.graphEdges}">
                            <line x1="${edge.sourceX}" y1="${edge.sourceY}" x2="${edge.targetX}" y2="${edge.targetY}" class="graph-line"/>
                            <text x="${(edge.sourceX + edge.targetX) / 2}" y="${(edge.sourceY + edge.targetY) / 2 - 8}" class="graph-edge-label">${edge.relationType}</text>
                        </c:forEach>
                        <c:forEach var="node" items="${analysisResult.graphNodes}">
                            <circle cx="${node.x}" cy="${node.y}" r="${node.key ? 28 : 23}" class="graph-node ${node.key ? 'graph-node-key' : ''}" filter="${node.key ? 'url(#glow)' : ''}"/>
                            <text x="${node.x}" y="${node.y + 45}" class="graph-node-label">${node.name}</text>
                        </c:forEach>
                    </svg>
                    <div class="graph-legend"><span><i class="legend-dot"></i>Person</span><span><i class="legend-dot key"></i>Priority actor</span><span>Edge label = relationship type</span></div>
                </div>
            </article>

            <article class="panel">
                <div class="panel-heading"><div><span class="section-kicker">PRIORITY ACTOR</span><h2>Key individual</h2></div></div>
                <div class="key-person-card">
                    <span class="key-person-label">Highest weighted score</span>
                    <strong>${analysisResult.keyPerson}</strong>
                    <span class="muted">Use this as an analytical lead, not as a guilt determination.</span>
                </div>
                <h3 class="subheading">Top influencers</h3>
                <div class="table-wrap"><table><thead><tr><th>Rank</th><th>Name</th><th>Degree</th><th>Weighted</th></tr></thead><tbody>
                    <c:forEach var="row" items="${analysisResult.topInfluencers}">
                        <tr><td>${row.rank}</td><td><strong>${row.name}</strong></td><td>${row.degree}</td><td>${row.weightedScore}</td></tr>
                    </c:forEach>
                    <c:if test="${empty analysisResult.topInfluencers}"><tr><td colspan="4" class="empty-row">No relationship ranking available.</td></tr></c:if>
                </tbody></table></div>
            </article>
        </section>

        <section class="content-grid two-thirds analysis-grid">
            <article class="panel">
                <div class="panel-heading"><div><span class="section-kicker">PATH ANALYSIS</span><h2>Connection discovered</h2></div></div>
                <p class="muted">Default trace: <strong>${analysisResult.pathA}</strong> → <strong>${analysisResult.pathB}</strong></p>
                <c:choose>
                    <c:when test="${not empty analysisResult.pathResults}">
                        <div class="path-list automated-path">
                            <c:forEach var="p" items="${analysisResult.pathResults}" varStatus="loop">
                                <div class="path-node"><span>${loop.index + 1}</span><strong>${p}</strong><em>${loop.index == 0 ? 'START' : (loop.last ? 'TARGET' : 'HOP')}</em></div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise><div class="empty-state">No connection path is currently available.</div></c:otherwise>
                </c:choose>
            </article>

            <article class="panel">
                <div class="panel-heading"><div><span class="section-kicker">EVIDENCE TRAIL</span><h2>${analysisResult.focusPerson} links</h2></div></div>
                <div class="table-wrap"><table><thead><tr><th>Target</th><th>Relation</th><th>Strength</th><th>Source</th></tr></thead><tbody>
                    <c:forEach var="row" items="${analysisResult.evidence}">
                        <tr><td><strong>${row.target}</strong></td><td>${row.relationType}</td><td>${row.strength}</td><td>${row.evidenceSource}</td></tr>
                    </c:forEach>
                    <c:if test="${empty analysisResult.evidence}"><tr><td colspan="4" class="empty-row">No direct evidence-linked relationships yet.</td></tr></c:if>
                </tbody></table></div>
            </article>
        </section>

        <section class="content-grid two-thirds analysis-grid">
            <article class="panel">
                <div class="panel-heading"><div><span class="section-kicker">ANALYTICAL INDICATORS</span><h2>What NETRA found</h2></div></div>
                <div class="indicator-list">
                    <c:forEach var="item" items="${analysisResult.indicators}">
                        <div class="indicator-row level-${item.level.toLowerCase()}"><span class="indicator-level">${item.level}</span><div><strong>${item.title}</strong><p>${item.detail}</p></div></div>
                    </c:forEach>
                </div>
            </article>

            <article class="panel">
                <div class="panel-heading"><div><span class="section-kicker">CASE SUMMARY</span><h2>Investigator briefing</h2></div></div>
                <div class="case-summary"><p>${analysisResult.caseSummary}</p></div>
                <div class="entity-chip-group">
                    <span class="entity-chip-title">Persons</span>
                    <c:forEach var="item" items="${analysisResult.persons}"><span class="entity-chip">${item}</span></c:forEach>
                </div>
                <div class="entity-chip-group">
                    <span class="entity-chip-title">Phones</span>
                    <c:forEach var="item" items="${analysisResult.phoneNumbers}"><span class="entity-chip">${item}</span></c:forEach>
                </div>
                <div class="entity-chip-group">
                    <span class="entity-chip-title">Locations</span>
                    <c:forEach var="item" items="${analysisResult.locations}"><span class="entity-chip">${item}</span></c:forEach>
                </div>
                <div class="entity-chip-group">
                    <span class="entity-chip-title">Vehicles</span>
                    <c:forEach var="item" items="${analysisResult.vehicleNumbers}"><span class="entity-chip">${item}</span></c:forEach>
                </div>
            </article>
        </section>
    </c:if>

    <section class="content-grid two-thirds">
        <article class="panel">
            <div class="panel-heading"><div><span class="section-kicker">NETWORK EXPLORER</span><h2>Expand suspect network</h2></div></div>
            <form action="${pageContext.request.contextPath}/admin/network" method="get" class="form-inline">
                <input type="text" name="expandName" placeholder="Suspect name" value="${expandName}" required>
                <button type="submit" class="btn">Expand</button>
            </form>
            <c:if test="${not empty expandName}">
                <h3 class="subheading">Neo4j — up to 2 hops from “${expandName}”</h3>
                <div class="table-wrap"><table><thead><tr><th>Name</th><th>Risk level</th><th>Influence</th></tr></thead><tbody>
                    <c:forEach var="p" items="${expandResults}"><tr><td><strong>${p.name}</strong></td><td>${p.riskLevel}</td><td>${p.influenceScore}</td></tr></c:forEach>
                    <c:if test="${empty expandResults}"><tr><td colspan="3" class="empty-row">No connected persons found.</td></c:if>
                </tbody></table></div>
                <h3 class="subheading">Java Collections — direct associates</h3>
                <div class="table-wrap"><table><thead><tr><th>Associate</th><th>Relation</th><th>Strength</th><th>Evidence</th></tr></thead><tbody>
                    <c:forEach var="a" items="${directAssociates}"><tr><td>${a.target}</td><td>${a.relationType}</td><td>${a.weight}</td><td>${a.evidenceSource}</td></tr></c:forEach>
                    <c:if test="${empty directAssociates}"><tr><td colspan="4" class="empty-row">No direct associates recorded in the in-memory graph.</td></c:if>
                </tbody></table></div>
            </c:if>
        </article>

        <article class="panel">
            <div class="panel-heading"><div><span class="section-kicker">MANUAL LINK ANALYSIS</span><h2>Find connection path</h2></div></div>
            <form action="${pageContext.request.contextPath}/admin/network" method="get" class="form-grid">
                <label>Person A</label><input type="text" name="pathA" placeholder="Rahul Sharma" value="${pathA}" required>
                <label>Person B</label><input type="text" name="pathB" placeholder="Vijay Kumar" value="${pathB}" required>
                <div class="form-actions"><button type="submit" class="btn">Trace path</button></div>
            </form>
            <c:if test="${not empty pathA}">
                <h3 class="subheading">Neo4j shortest path</h3>
                <div class="path-list">
                    <c:forEach var="p" items="${pathResults}" varStatus="loop">
                        <div class="path-node"><span>${loop.index + 1}</span><strong>${p.name}</strong><em>${p.riskLevel}</em></div>
                    </c:forEach>
                </div>
                <c:if test="${empty pathResults}"><div class="empty-state">No path found between the selected people.</div></c:if>
            </c:if>
        </article>
    </section>

    <article class="panel">
        <div class="panel-heading"><div><span class="section-kicker">PRIORITY ACTORS</span><h2>Top influencers</h2></div><span class="muted">Degree centrality · Java Collections</span></div>
        <div class="ranking-list compact">
            <c:forEach var="entry" items="${topInfluencers}" varStatus="loop">
                <div class="rank-row"><span class="rank">${loop.index + 1}</span><div class="rank-person"><strong>${entry.key}</strong><span>Direct relationship count</span></div><span class="rank-score">${entry.value}</span></div>
            </c:forEach>
            <c:if test="${empty topInfluencers}"><div class="empty-state">No relationship data available yet. Create or ingest a network relationship above.</div></c:if>
        </div>
    </article>
</main>
</body>
</html>
