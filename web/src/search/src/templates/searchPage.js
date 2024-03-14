import _ from 'underscore'

/*
 * This underscore template generates the search results client side.
 *
 * IMPORTANT: If you change the structure of this, please update the
 * corresponding freemarker template /templates/search/_page.ftl
 */
export default _.template(`
<div class="results__header">
    <span id="num-records"><%=numFound%></span> records found
</div>

<div class="results__related_searches"></div>

<div class="results__list">
<% _.each(results, function(result) { %>

    <a class="result result--<%=result.state%> <% if (result.resourceStatus != '') { %>result--<%=result.resourceStatus%><% } %>" id="<%=result.identifier%>" href="/documents/<%=result.identifier%>">

        <div class="result__tags">
            <div class="recordType">
            <% if (result.documentType != '' && result.documentType == "LINK_DOCUMENT") {  %>
            <i class="fa-solid fa-link"></i> Linked
            <% } %>
                <span><%=result.recordType%></span>
            </div>

            <% if (result.resourceStatus != '') {  %>
                <div class="resourceStatus"><%=result.resourceStatus%></div>
            <% } %>

            <div class="state">
                <% if(result.state == 'draft') { %>
                    <span class="text-draft"><b>DRAFT</b></span>
                <% } else if(result.state == 'pending') { %>
                    <span class="text-pending"><b>PENDING PUBLICATION</b></span>
                <% } %>
            </div>

            <% if (typeof result.condition != "undefined" && result.condition != '') {  %>
                <div class="condition"><%=result.condition%></div>
            <% } %>
        </div>

        <div class="result__title"><%=result.title%></div>
        <div class="result__description"><%=result.shortenedDescription%></div>

        <% if(result.incomingCitationCount != 0) { %>
            <div class="result__citationCount"><%=result.incomingCitationCount%> citation<% if(result.incomingCitationCount >1) { %>s<% } %></div>
        <% } %>

    </a>
<% }); %>
</div>

<div class="results__footer">
<ul class="pager">
    <% if(prevPage) { %>
        <li class="previous"><a href="<%=prevPage%>">&larr; Previous</a></li>
    <% } %>
    <li>Page <%=page%></li>
    <% if(nextPage) { %>
        <li class="next"><a href="<%=nextPage%>">Next &rarr;</a></li>
    <% } %>
</ul>
</div>
`)
